package com.quochuy.store.controller;

import com.quochuy.store.dto.request.CartItemRequest;
import com.quochuy.store.dto.request.CartRequest;
import com.quochuy.security.CustomUserDetails;
import com.quochuy.store.service.CartMutationResult;
import com.quochuy.store.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/api/backend")
public class CartController {
	@Autowired
	CartService cartService;
	
	/**
	 * Lấy số lượng sản phẩm trong giỏ hàng. *
	 * @PreAuthorize("isAuthenticated()"): Spring sẽ chặn
	 * ngay từ vòng gửi xe nếu Token không hợp lệ hoặc không
	 * có Token. Do đó, userDetails bên dưới đảm bảo luôn
	 * KHÁC NULL.
	 */
	@GetMapping("/cart/count")
	public ResponseEntity<?> getCartCount(
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		/* Nhờ @PreAuthorize, ta không cần: if (userDetails == null) { ... }*/
		//		if (userDetails == null) {
		//			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		//		}
		// Bây giờ bạn có thể gọi getUserId() trực tiếp vì đã khai báo đúng kiểu CustomUserDetails
		UUID userId = userDetails.getUserId();
		int count = cartService.countItemsByUsername(
				userId);
		return ResponseEntity.ok(
				Map.of("cartCount", count));
	}
	
	@PostMapping("/cart/add")
	public ResponseEntity<?> addCart(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestBody CartRequest cartRequest) {
		UUID userId = userDetails.getUserId();
		cartService.addProductToCart(userId,
									 UUID.fromString(cartRequest.getProductId()),
									 cartRequest.getAmount());
		// Logic dành riêng cho Admin
		return ResponseEntity.ok(
				Map.of("systemTotal", 9999));
	}
	
	@PatchMapping("/cart/update-item-cart")
	public ResponseEntity<?> updateItemCart(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestBody CartItemRequest body) {
		CartMutationResult result = cartService.updateItemCart(
				userDetails.getUserId(),
				UUID.fromString(body.getCartItemId()),
				body.getAmount(),
				body.getUpdatedAt());
		if (result == CartMutationResult.NOT_FOUND) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", "Cart item not found"));
		}
		if (result == CartMutationResult.CONFLICT) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(Map.of("message", "Cart item changed, please reload cart"));
		}
		return ResponseEntity.ok(
				Map.of("systemTotal", 9999));
	}
	
	@GetMapping("/cart/details")
	public ResponseEntity<?> fetchCartDetails(
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		UUID userId = userDetails.getUserId();
		return cartService.fetchCartDetails(userId)
				.<ResponseEntity<?>>map(cart ->ResponseEntity.ok(Map.of("cartDetails", cart)))
				.orElseGet(() ->ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of("message", (Object) "Cart not found"))
				);
	}
	
	@DeleteMapping("/cart/remove/{cartItemId}")
	public ResponseEntity<Void> removeItemCart(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable String cartItemId) {
		boolean removed = cartService.removeItemCard(
				userDetails.getUserId(),
				UUID.fromString(cartItemId));
		if (!removed) {
			return ResponseEntity.notFound()
					.build();
		}
		return ResponseEntity.noContent()
				.build();// status = 204 200->299 thì đều .ok()
	}
	
	/**
	 * Ví dụ: Chỉ Admin mới có quyền xem tổng số lượng giỏ
	 * hàng của toàn hệ thống
	 */
	@GetMapping("/admin/total-count")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> getTotalSystemCartCount() {
		// Logic dành riêng cho Admin
		return ResponseEntity.ok(
				Map.of("systemTotal", 9999));
	}
}
