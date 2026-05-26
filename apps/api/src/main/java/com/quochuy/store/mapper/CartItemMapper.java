package com.quochuy.store.mapper;

import com.quochuy.store.model.CartItem;
import com.quochuy.store.model.CartItemJoinProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Mapper
public interface CartItemMapper {
	CartItem fetchCartItem(UUID productId);
	
	List<CartItemJoinProduct> fetchItemsByCart(UUID cartId);
	
	void createCartItem(UUID cartId, UUID productId, int amount);
	
	int updateCartItem(@Param("userId") UUID userId,
					   @Param("cartItemId") UUID cartItemId,
					   @Param("amount") int amount,
					   @Param("updatedAt") OffsetDateTime updatedAt);

	int countCartItemByUser(@Param("userId") UUID userId,
							@Param("cartItemId") UUID cartItemId);
	
	void upsertCartItem(@Param("cartId") UUID cartId,
						@Param("productId") UUID productId,
						@Param("amount") int amount);
	
	int removeItemCard(@Param("userId") UUID userId,
					   @Param("cartItemId") UUID cartItemId);
}
