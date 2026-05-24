package com.quochuy.store.dto.request;

import lombok.Data;

@Data
public class CartItemRequest {
	String cartItemId;
	int amount;
}
