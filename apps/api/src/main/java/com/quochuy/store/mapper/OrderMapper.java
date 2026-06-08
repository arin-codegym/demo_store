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
	
	Order createOrGetPendingOrder(
			@Param("productsCount") int productsCount,
			@Param("orderTotal") int orderTotal,
			@Param("tax") int tax,
			@Param("shipping") int shipping,
			@Param("email") String email,
			@Param("userId") UUID userId,
			@Param("cartId") UUID cartId,
			@Param("key") String key
	);
	
	Optional<Order> findByIdempotencyKey(String key);
	
	
	Order findByOrderId(@Param("orderId") UUID orderId);
	
	Order findByOrderIdForUpdate(@Param("orderId") UUID orderId);
	
	List<Order> findByOrderIdIsPaid(UUID userId);
	
	
	List<Order> getDashboardOrder();
	
	Optional<Order> findPendingByUserId(@Param("userId") UUID userId);
	
	Optional<Order> findByOrderIdAndUserId(
			@Param("orderId") UUID orderId,
			@Param("userId") UUID userId
	);
	
	int insertOrder(Order order);
	
	int recalculateOrderTotalsFromItems(@Param("orderId") UUID orderId);
	
	UUID  markPendingOrderPaid(@Param("orderId") UUID orderId);
	
	int saveStripeSessionId(
			@Param("orderId") UUID orderId,
			@Param("stripeSessionId") String stripeSessionId
	);
}
