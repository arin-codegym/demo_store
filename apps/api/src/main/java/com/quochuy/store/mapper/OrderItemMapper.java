package com.quochuy.store.mapper;

import com.quochuy.store.model.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface OrderItemMapper {
	int copyFromCartItems(
			@Param("orderId") UUID orderId,
			@Param("cartId") UUID cartId
	);
	
	List<OrderItem> findByOrderId(@Param("orderId") UUID orderId);
}
