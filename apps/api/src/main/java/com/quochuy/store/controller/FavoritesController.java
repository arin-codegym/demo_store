package com.quochuy.store.controller;

import com.quochuy.store.record.FavoriteItem;
import com.quochuy.security.CustomUserDetails;
import com.quochuy.store.service.impl.FavoritesServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/backend")
@RequiredArgsConstructor
public class FavoritesController {
	private final FavoritesServiceImpl favoritesServiceImpl;
	
	@PostMapping("/favorites")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Map<String, UUID>> createFavorite(@RequestBody UUID productId,
															@AuthenticationPrincipal CustomUserDetails customUserDetails) {
		return ResponseEntity.ok(Map.of("favoriteId", favoritesServiceImpl.create(productId,
																				  customUserDetails.getUserId())));
	}
	
	@DeleteMapping("/favorites/{favoriteId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Map<String, String>> deleteFavorite(@PathVariable UUID favoriteId,
															  @AuthenticationPrincipal CustomUserDetails customUserDetails) {
		 favoritesServiceImpl.delete(favoriteId, customUserDetails.getUserId());
		return ResponseEntity.ok(Map.of("message", String.format("Delete success %s", favoriteId)));
	}
	@GetMapping("/favorites/check/{productId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> fetchFavoriteId(@PathVariable UUID productId,
											 @AuthenticationPrincipal CustomUserDetails userDetails) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		System.out.println(auth.getPrincipal().getClass());
		UUID userId = userDetails.getUserId();
		String favoriteId = favoritesServiceImpl.fetchFavoriteId(userId, productId ).orElse("");
		return ResponseEntity.ok(Map.of("favoriteId", favoriteId));
	}
	
	@GetMapping("/favorites/fetchUserFavorites")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> fetchUserFavorites( @AuthenticationPrincipal CustomUserDetails userDetails) {
		UUID userId = userDetails.getUserId();
		List<FavoriteItem> favoriteItemList = favoritesServiceImpl.fetchUserFavorites(userId);
		return ResponseEntity.ok(Map.of("userFavorites", favoriteItemList));
	}

}
