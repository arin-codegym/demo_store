package com.quochuy.store.service.impl;

import com.quochuy.store.dto.OrderDto;
import com.quochuy.store.mapper.CartMapper;
import com.quochuy.store.mapper.OrderMapper;
import com.quochuy.store.model.Cart;
import com.quochuy.store.model.Order;
import com.quochuy.store.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
	private final CartMapper cartMapper;
	private final OrderMapper orderMapper;
	
	@Override
	@Transactional
	public OrderDto createOrder(UUID userId,
								String email,
								String key) {
		Optional<Order> existing = orderMapper.findPendingByUserId(
				userId);
		if (existing.isPresent()) {
			return new OrderDto(existing.get().getOrderId().toString(),
								existing.get().getCartId().toString());
		}
		Cart cart = cartMapper.fetchCartByUser(userId);
		Order order = orderMapper.createOrder(
				cart.getNumItemsInCart(),
				cart.getOrderTotal(),
				cart.getTax(),
				cart.getShipping(),
				email,
				userId,
				cart.getCartId(),
				key);
		return new OrderDto(order.getOrderId().toString(),
							cart.getCartId().toString());
	}
	
	@Override
	public OrderDto exitsOrder(String key) {
		return orderMapper.findByIdempotencyKey(key)
				.map(order -> new OrderDto(
						order.getOrderId().toString(),
						order.getCartId().toString()))
				.orElseThrow(
						() -> new IllegalStateException(
								"Order not found for idempotency key"));
	}
	
	@Override
	public List<Order> getPaidOrderByUserId(UUID userId) {
		return orderMapper.findByOrderIdIsPaid(userId);
	}
}
