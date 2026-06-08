package com.quochuy.store.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class Order {
	private UUID orderId;
	private int productsCount;
	private int orderTotal;
	private int tax;
	private int shipping;
	private String email;
	private String status;
	private UUID userId;
	private UUID cartId;
	private UUID sourceCartId;
	private String idempotencyKey;
	private String currency;
	private String stripeSessionId;
	OffsetDateTime createdAt;
	OffsetDateTime updatedAt;
	OffsetDateTime paidAt;
}
