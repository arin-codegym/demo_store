package com.quochuy.store.mapper;

import com.quochuy.store.model.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface CartMapper {
	Optional<Integer> countItemsByUsername(@Param("userId") UUID userId);
	
	Cart fetchCartByUser(@Param("userId") UUID userId);
	
	Optional<Cart> findByCartId(UUID cardId);
	
	void createCart(@Param("userId") UUID userId);
	
	void clearCart(UUID cartId);
}
