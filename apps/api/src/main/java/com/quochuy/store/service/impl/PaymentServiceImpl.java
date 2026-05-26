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
import com.stripe.net.RequestOptions;
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
	private final OrderMapper orderMapper;
	private final CartMapper cartMapper;
	@Value("${stripe.secret.webhook-secret}")
	private String endpointSecret;
	@Value("${app.frontend-url}")
	private String domain;
	
	public PaymentResponse createCheckoutSession(PaymentRequest request) {
		try {
			UUID orderId = UUID.fromString(request.getOrderId());
			UUID requestedCartId = UUID.fromString(request.getCartId());
			Order order = orderMapper.findByOrderId(orderId);
			if (order == null) {
				throw new IllegalStateException("Order not found");
			}
			if (!OrderStatus.PENDING.name().equals(order.getStatus())) {
				throw new IllegalStateException("Order not in PENDING state");
			}
			if (!order.getCartId().equals(requestedCartId)) {
				throw new IllegalStateException("Cart does not belong to order");
			}
			
			Cart cart = cartMapper.findByCartId(order.getCartId())
					.orElseThrow(() -> new RuntimeException("Cart not found"));
			List<SessionCreateParams.LineItem> lineItems = buildLineItemsFromCart(cart);
			
			SessionCreateParams params = SessionCreateParams.builder()
					.setMode(SessionCreateParams.Mode.PAYMENT)
					.setUiMode(SessionCreateParams.UiMode.EMBEDDED)
					.putMetadata("orderId", order.getOrderId().toString())
					.putMetadata("cartId", order.getCartId().toString())
					.setReturnUrl(domain + "/orders?session_id={CHECKOUT_SESSION_ID}")
					.addAllLineItem(lineItems)
					.build();
			RequestOptions requestOptions = RequestOptions.builder()
					.setIdempotencyKey("checkout-session:" + order.getOrderId())
					.build();
			Session session = stripeClient.v1().checkout().sessions()
					.create(params, requestOptions);
			return new PaymentResponse(session.getClientSecret());
		} catch (StripeException e) {
			throw new RuntimeException("Stripe error: " + e.getMessage(), e);
		}
	}
	
	public ResponseEntity<String> handleWebhook(String payload, String sigHeader) {
		Event event;
		try {
			event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
		}
		
		if ("checkout.session.completed".equals(event.getType())) {
			Session session = (Session) event.getDataObjectDeserializer()
					.getObject()
					.orElseThrow();
			markPaidAndClearCartOnce(session);
		}
		return ResponseEntity.ok("success");
	}
	
	private List<SessionCreateParams.LineItem> buildLineItemsFromCart(Cart cart) {
		return cart.getCartItems().stream()
				.map(item -> SessionCreateParams.LineItem.builder()
						.setQuantity((long) item.getAmount())
						.setPriceData(SessionCreateParams.LineItem.PriceData.builder()
								.setCurrency("usd")
								.setUnitAmount(item.getProduct().getPrice() * 100L)
								.setProductData(
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
			Session session = stripeClient
					.v1()
					.checkout()
					.sessions()
					.retrieve(sessionId);
			if (!"paid".equals(session.getPaymentStatus())) {
				return ResponseEntity.badRequest().body("Payment not completed");
			}
			UUID orderId = markPaidAndClearCartOnce(session);
			return ResponseEntity.ok(orderId);
		} catch (StripeException e) {
			throw new RuntimeException("Stripe verify error: " + e.getMessage(), e);
		}
	}
	
	private UUID markPaidAndClearCartOnce(Session session) {
		if (!"paid".equals(session.getPaymentStatus())) {
			return UUID.fromString(session.getMetadata().get("orderId"));
		}
		UUID orderId = UUID.fromString(session.getMetadata().get("orderId"));
		UUID cartId = orderMapper.markPendingOrderPaid(orderId);
		if (cartId != null) {
			cartMapper.clearCart(cartId);
		}
		return orderId;
	}
}
