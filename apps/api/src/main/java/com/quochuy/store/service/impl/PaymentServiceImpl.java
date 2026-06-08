package com.quochuy.store.service.impl;

import com.quochuy.store.dto.PaymentRequest;
import com.quochuy.store.dto.PaymentResponse;
import com.quochuy.store.enums.OrderStatus;
import com.quochuy.store.mapper.CartMapper;
import com.quochuy.store.mapper.OrderItemMapper;
import com.quochuy.store.mapper.OrderMapper;
import com.quochuy.store.model.Cart;
import com.quochuy.store.model.Order;
import com.quochuy.store.model.OrderItem;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl {
	private final StripeClient stripeClient;
	private final OrderMapper orderMapper;
	private final CartMapper cartMapper;
	private final OrderItemMapper orderItemMapper;
	
	@Value("${stripe.secret.webhook-secret}")
	private String endpointSecret;
	@Value("${app.frontend-url}")
	private String domain;
	
	public PaymentResponse createCheckoutSession(PaymentRequest request) {
		try {
			UUID orderId = UUID.fromString(request.getOrderId());
			
			Order order = orderMapper.findByOrderIdForUpdate(orderId);
			if (order == null) {
				throw new IllegalStateException("Order not found");
			}
			
			if (!OrderStatus.PENDING.name().equals(order.getStatus())) {
				throw new IllegalStateException("Order not in PENDING state");
			}
			
			if (order.getStripeSessionId() != null && !order.getStripeSessionId().isBlank()) {
				Session existingSession = stripeClient.v1()
						.checkout()
						.sessions()
						.retrieve(order.getStripeSessionId());
				
				return new PaymentResponse(existingSession.getClientSecret());
			}
			
			List<OrderItem> orderItems = orderItemMapper.findByOrderId(orderId);
			if (orderItems == null || orderItems.isEmpty()) {
				throw new IllegalStateException("Order has no items");
			}
			
			List<SessionCreateParams.LineItem> lineItems =
					buildLineItemsFromOrderItems(orderItems);
			
			SessionCreateParams params = SessionCreateParams.builder()
					.setMode(SessionCreateParams.Mode.PAYMENT)
					.setUiMode(SessionCreateParams.UiMode.EMBEDDED)
					.putMetadata("orderId", order.getOrderId().toString())
					.setReturnUrl(domain + "/orders?session_id={CHECKOUT_SESSION_ID}")
					.addAllLineItem(lineItems)
					.build();
			
			RequestOptions requestOptions = RequestOptions.builder()
					.setIdempotencyKey("checkout-session:" + order.getOrderId())
					.build();
			
			Session session = stripeClient.v1()
					.checkout()
					.sessions()
					.create(params, requestOptions);
			
			orderMapper.saveStripeSessionId(orderId, session.getId());
			
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
	
	private List<SessionCreateParams.LineItem> buildLineItemsFromOrderItems(List<OrderItem> orderItems) {
		return orderItems.stream()
				.map(item -> {
					SessionCreateParams.LineItem.PriceData.ProductData.Builder productDataBuilder =
							SessionCreateParams.LineItem.PriceData.ProductData.builder()
									.setName(item.getProductName());
					
					if (item.getProductImage() != null && !item.getProductImage().isBlank()) {
						productDataBuilder.addImage(item.getProductImage());
					}
					
					return SessionCreateParams.LineItem.builder()
							.setQuantity((long) item.getAmount())
							.setPriceData(
									SessionCreateParams.LineItem.PriceData.builder()
											.setCurrency("usd")
											.setUnitAmount(item.getUnitPrice() * 100L)
											.setProductData(productDataBuilder.build())
											.build()
							)
							.build();
				})
				.toList();
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
	
//	public ResponseEntity<?> verifyPayment(String sessionId) {
//		try {
//			Session session = stripeClient
//					.v1()
//					.checkout()
//					.sessions()
//					.retrieve(sessionId);
//			if (!"paid".equals(session.getPaymentStatus())) {
//				return ResponseEntity.badRequest().body("Payment not completed");
//			}
//			UUID orderId = markPaidAndClearCartOnce(session);
//			return ResponseEntity.ok(orderId);
//		} catch (StripeException e) {
//			throw new RuntimeException("Stripe verify error: " + e.getMessage(), e);
//		}
//	}
	
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
			
			UUID orderId = markPaidAndCheckoutCartOnce(session);
			
			return ResponseEntity.ok(orderId);
			
		} catch (StripeException e) {
			throw new RuntimeException("Stripe verify error: " + e.getMessage(), e);
		}
	}
	
	
	
	@Transactional
	public UUID markPaidAndClearCartOnce(Session session) {
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
	
	@Transactional
	public UUID markPaidAndCheckoutCartOnce(Session session) {
		UUID orderId = UUID.fromString(session.getMetadata().get("orderId"));
		
		if (!"paid".equals(session.getPaymentStatus())) {
			return orderId;
		}
		
		Order order = orderMapper.findByOrderId(orderId);
		if (order == null) {
			throw new IllegalStateException("Order not found");
		}
		
		/*
		 * Idempotent:
		 * Nếu webhook và verify cùng chạy,
		 * chỉ request đầu tiên update được status PENDING -> PAID.
		 */
		UUID sourceCartId = orderMapper.markPendingOrderPaid(orderId);
		if (Objects.nonNull(sourceCartId)) {
			cartMapper.markCartCheckedOut(order.getSourceCartId());
		}
		
		return orderId;
	}
}
