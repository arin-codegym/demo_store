package com.quochuy.store.mapper;

import com.quochuy.store.model.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface OrderMapper {
	Order createOrder(int productsCount, int orderTotal, int tax, int shipping, String email,
					  @Param("userId") UUID userId, UUID cartId, String key);
	
	Optional<Order> findByIdempotencyKey(String key);
	
	Optional<Order> findPendingByUserId(UUID userId);
	
	Order findByOrderId(UUID orderId);
	
	List<Order> findByOrderIdIsPaid(UUID userId);
	
	UUID markPendingOrderPaid(@Param("orderId") UUID orderId);
	
	List<Order> getDashboardOrder();
}
