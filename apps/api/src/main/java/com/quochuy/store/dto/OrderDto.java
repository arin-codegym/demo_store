package com.quochuy.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderDto {
	private String orderId;
	private String cartId;
}
