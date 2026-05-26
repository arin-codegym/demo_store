package com.quochuy.store.service;

import com.quochuy.store.model.Cart;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface CartService {
	int countItemsByUsername(UUID userId);
	
	Cart addProductToCart(UUID userId, UUID productId,
						  int amount);
	

	boolean removeItemCard(UUID userId, UUID cartItemId);
	
	Optional<Cart> fetchCartDetails(UUID userId);
	
	CartMutationResult updateItemCart(UUID userId,
									  UUID cartItemId,
									  int amount,
									  OffsetDateTime updatedAt);
	
	Cart findById(UUID cartId);
	
	void clearCart(UUID cartId);
}
