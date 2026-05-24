package com.quochuy.store.service.impl;

import com.quochuy.store.mapper.CartItemMapper;
import com.quochuy.store.mapper.CartMapper;
import com.quochuy.store.model.Cart;
import com.quochuy.store.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
	private final CartMapper cartMapper;
	private final CartItemMapper cartItemMapper;
	
	@Override
	public Optional<Integer> countItemsByUsername(
			UUID userId) {
		return cartMapper.countItemsByUsername(userId)
				.orElse(0).describeConstable();
	}
	
	@Override
		/*
		không cần throw Exception.class vì trong body chẳn có
		cái nào gây ra checked exception nếu muốn throw
		exception thì bắt buộc phải throw ngoài controller
		hoặc catch . Không throws lỗi chung chung như thế.
		ở đây chắc chắn chỉ throw RuntimeException.
		RuntimeException tự động thoát ra khỏi hàm nên
		không cần try catch throw => có GlobalException
		lo rồi
	*/
//	@Transactional(rollbackFor = Exception.class)
	@Transactional
	public Cart addProductToCart(UUID userId,
								 UUID productId,
								 int amount) {
		Cart cart = cartMapper.fetchCartByUser(userId);
		/*Create cart if not exits*/
		if (Objects.isNull(cart)) {
			cartMapper.createCart(userId);
			cart = cartMapper.fetchCartByUser(userId);
		}
		//		CartItem cartItem = cartItemMapper.fetchCartItem(productId);
		//		/*create or update cart item*/
		//		if (Objects.nonNull(cartItem)) {
		//			int amountCount = cartItem.getAmount() + amount;
		//			cartItemMapper.updateCartItem(cartItem.getCart_item_id(), amountCount);
		//		} else {
		//			cartItemMapper.createCartItem(cart.getCart_id(), productId, amount);
		//		}
		//		/*update cart*/
		//		List<CartItemJoinProduct> fetchItemsByCart = cartItemMapper.fetchItemsByCart(cart.getCart_id());
		//		int numItemsInCart = 0;
		//		int cartTotal = 0;
		//		for (CartItemJoinProduct item : fetchItemsByCart) {
		//			numItemsInCart += item.getAmount();
		//			cartTotal += item.getAmount() * item.getPrice();
		//		}
		//		Float tax = cart.getTaxRate() * cartTotal;
		//		int shipping = cartTotal > 0 ? cart.getShipping() : 0;
		//		Float orderTotal = cartTotal + tax + shipping;
		//		cartMapper.updateCart(numItemsInCart, cartTotal, tax.intValue(), orderTotal.intValue());
		// 2. Thực hiện UPSERT vào bảng cart_items
		// Ngay khi lệnh này chạy xong, Trigger 'trg_after_cart_item_change'
		// dưới Supabase sẽ TỰ ĐỘNG tính lại bảng 'carts'.
		cartItemMapper.upsertCartItem(cart.getCartId(),
									  productId, amount);
		// 3. LẤY KẾT QUẢ MỚI NHẤT (Quan trọng)
		// Vì Trigger chạy trong cùng Transaction, nên khi bạn fetch lại ngay tại đây,
		// bạn sẽ nhận được Cart đã có đầy đủ num_items_in_cart, cart_total, order_total...
		return cartMapper.fetchCartByUser(userId);
	}
	
	//	@Transactional(rollbackFor = Exception.class)
	@Transactional
	@Override
	public void updateItemCart(UUID cartItemId,
							   int amount) {
		cartItemMapper.updateCartItem(cartItemId, amount);
	}
	
	@Override
	public Cart findById(UUID cartId) {
		return null;
	}
	
	@Override
	public void clearCart(UUID cartId) {
	}
	
	@Transactional
	@Override
	public void removeItemCard(UUID cartItemId) {
		cartItemMapper.removeItemCard(cartItemId);
	}
	
	@Override
	public Optional<Cart> fetchCartDetails(UUID userId) {
		return Optional.ofNullable(cartMapper.fetchCartByUser(userId));
	}
}
/*@Override
@Transactional(rollbackFor = Exception.class) // Vẫn giữ để bảo vệ dữ liệu tuyệt đối
public Cart addProductToCart(String userId, String productId, int amount, int price) {
    try {
        // 1. Lấy giỏ hàng
        Cart cart = cartMapper.fetchCartByUser(userId);

        // 2. Tạo giỏ hàng nếu chưa có
        if (Objects.isNull(cart)) {
            cartMapper.createCart(userId);
            cart = cartMapper.fetchCartByUser(userId);
        }

        // 3. Thực hiện UPSERT (Trigger trên Supabase sẽ tự tính toán)
        cartItemMapper.upsertCartItem(cart.getCart_id(), productId, amount);

        // 4. Lấy kết quả cuối cùng trả về
        return cartMapper.fetchCartByUser(userId);

    } catch (Exception e) {
        // Nếu có bất kỳ lỗi gì (kể cả Checked lỗi logic),
        // ta bọc lại thành Runtime để Spring thực hiện Rollback và Java không bắt khai báo 'throws'
        throw new RuntimeException("Lỗi xử lý giỏ hàng: " + e.getMessage(), e);
    }
}*/