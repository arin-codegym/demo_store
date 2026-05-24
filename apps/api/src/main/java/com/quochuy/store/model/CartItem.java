package com.quochuy.store.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class CartItem {
	UUID cartItemId;
	int amount;
	OffsetDateTime createdAt;
	OffsetDateTime updatedAt;
	UUID productId;
	UUID cartId;
	Product product;
}
