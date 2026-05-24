package com.quochuy.store.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class Cart {
	UUID cartId;
	int numItemsInCart;
	int cartTotal;
	int shipping;
	int tax;
	float taxRate;
	int orderTotal;
	OffsetDateTime createdAt;
	OffsetDateTime updatedAt;
	UUID userId;
	List<CartItem> cartItems;
}
