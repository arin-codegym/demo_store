package com.quochuy.store.service;

import com.quochuy.store.dto.OrderDto;
import com.quochuy.store.model.Order;

import java.util.List;
import java.util.UUID;

public interface OrderService {
	OrderDto createOrder(UUID userId, String email,
						 String key);
	
	OrderDto getOrderDetail(UUID orderId, UUID userId);
	
	OrderDto exitsOrder(String key);
	
	List<Order> getPaidOrderByUserId(UUID userId);

}
