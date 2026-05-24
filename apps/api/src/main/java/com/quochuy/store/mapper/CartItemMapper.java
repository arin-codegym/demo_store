package com.quochuy.store.mapper;

import com.quochuy.store.model.CartItem;
import com.quochuy.store.model.CartItemJoinProduct;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface CartItemMapper {
	CartItem fetchCartItem(UUID productId);
	
	List<CartItemJoinProduct> fetchItemsByCart(UUID cartId);
	
	void createCartItem(UUID cartId, UUID productId, int amount);
	
	void updateCartItem(UUID cartItemId, int amount);
	
	void upsertCartItem(UUID cartId, UUID productId, int amount);
	
	void removeItemCard(UUID cartItemId);
}
