package com.quochuy.store.service;

import com.quochuy.store.model.Cart;

import java.util.Optional;
import java.util.UUID;

public interface CartService {
	Optional<Integer> countItemsByUsername(UUID userId);
	
	Cart addProductToCart(UUID userId, UUID productId,
						  int amount) throws Exception;
	

	void removeItemCard(UUID cartItemId);
	
	Optional<Cart> fetchCartDetails(UUID userId);
	
	void updateItemCart(UUID cartItemId, int amount);
	
	Cart findById(UUID cartId);
	
	void clearCart(UUID cartId);
}
