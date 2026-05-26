package com.quochuy.store.dto.request;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CartItemRequest {
	String cartItemId;
	int amount;
	OffsetDateTime updatedAt;
}
