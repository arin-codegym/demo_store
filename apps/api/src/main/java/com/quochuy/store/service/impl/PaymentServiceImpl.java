package com.quochuy.store.service.impl;

import com.quochuy.store.dto.PaymentRequest;
import com.quochuy.store.dto.PaymentResponse;
import com.quochuy.store.enums.OrderStatus;
import com.quochuy.store.mapper.CartMapper;
import com.quochuy.store.mapper.OrderMapper;
import com.quochuy.store.model.Cart;
import com.quochuy.store.model.Order;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl {
	private final StripeClient stripeClient;
	@Value("${stripe.secret.webhook-secret}")
	private String endpointSecret;
	@Value("${app.frontend-url}")
	private String domain;
	private final OrderMapper orderMapper;
	private final CartMapper cartMapper;
	
	public PaymentResponse createCheckoutSession(PaymentRequest request) {
		try {
			Order order = orderMapper.findByOrderId(UUID.fromString(request.getOrderId()));
			if (!order.getStatus().equals(OrderStatus.PENDING.name())) {
				throw new IllegalStateException("Order not in PENDING state");
			}
			Cart cart = cartMapper.findByCartId(UUID.fromString(request.getCartId()))
					.orElseThrow(() -> new RuntimeException("Cart not found"));
			List<SessionCreateParams.LineItem> lineItems = buildLineItemsFromCart(
					cart);
			/*PaymentIntentCreateParams
				→ dùng khi bạn tự build toàn bộ flow
				SessionCreateParams
				→ dùng khi Stripe lo gần hết*/
			SessionCreateParams params = SessionCreateParams.builder()
					.setMode(SessionCreateParams.Mode.PAYMENT)
					.setUiMode(SessionCreateParams.UiMode.EMBEDDED)
					.putMetadata("orderId", order.getOrderId().toString()) // 👈 QUAN TRỌNG
					.setReturnUrl(domain+"/orders?session_id={CHECKOUT_SESSION_ID}")
					.addAllLineItem(lineItems).build();
//		Session session = Session.create(params);// không
//		dùng static method
			Session session = stripeClient.v1().checkout().sessions()
					.create(params);
			return new PaymentResponse(session.getClientSecret());
		} catch (StripeException e) {
			throw new RuntimeException("Stripe error: " + e.getMessage());
		}
	}
	
	public ResponseEntity<String> handleWebhook(String payload,
												String sigHeader) {
		Event event;
		try {
			event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Invalid signature");
		}
//		if ("payment_intent.succeeded".equals(event.getType())) {
		if ("checkout.session.completed".equals(event.getType())) {
			// xử lý PAID
			Session session = (Session) event.getDataObjectDeserializer()
					.getObject().orElseThrow();
			UUID orderId = UUID.fromString(session.getMetadata().get("orderId"));
			Order order = orderMapper.findByOrderId(orderId);
//			order.setStatus(OrderStatus.PAID.name());
			orderMapper.updateOrderToPaid(order.getOrderId());
			cartMapper.clearCart(order.getCartId());
		}
		return ResponseEntity.ok("success");
	}
	
	private List<SessionCreateParams.LineItem> buildLineItemsFromCart(
			Cart cart) {
		return cart.getCartItems().stream()
				.map(item -> SessionCreateParams.LineItem.builder()
						.setQuantity((long) item.getAmount()).setPriceData(
								SessionCreateParams.LineItem.PriceData.builder()
										.setCurrency("usd").setUnitAmount(item.getProduct().getPrice() * 100L
												// cents
										).setProductData(
												SessionCreateParams.LineItem.PriceData.ProductData.builder()
														.setName(item.getProduct().getName())
														.addImage(item.getProduct().getImage())
														.build())
										.build())
						.build())
				.toList();
	}
	
	public ResponseEntity<?> verifyPayment(String sessionId) {
		try {
			
			// 1️⃣ Lấy session từ Stripe
			Session session = stripeClient
					.v1()
					.checkout()
					.sessions()
					.retrieve(sessionId);
			
			// 2️⃣ Check trạng thái thanh toán
			if (!"paid".equals(session.getPaymentStatus())) {
				return ResponseEntity
						.badRequest()
						.body("Payment not completed");
			}
			
			// 3️⃣ Lấy orderId từ metadata
			UUID orderId = UUID.fromString(session.getMetadata().get("orderId")) ;
			
			Order order = orderMapper.findByOrderId(orderId);
			
			// 4️⃣ Nếu chưa paid thì update
			if (!OrderStatus.PAID.name().equals(order.getStatus())) {
				order.setStatus(OrderStatus.PAID.name());
//				orderMapper.update(order);
			}
			
			return ResponseEntity.ok(orderId);
			
		} catch (StripeException e) {
			throw new RuntimeException("Stripe verify error: " + e.getMessage());
		}
	}
}
