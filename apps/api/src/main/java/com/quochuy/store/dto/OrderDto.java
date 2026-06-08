package com.quochuy.store.dto;

import com.quochuy.store.model.Order;
import com.quochuy.store.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class OrderDto {
	private UUID orderId;
	private UUID cartId;
	
	public static OrderDto from(Order order, List<OrderItem> items) {
		return new OrderDto(order.getOrderId(), order.getSourceCartId());
	}
}
