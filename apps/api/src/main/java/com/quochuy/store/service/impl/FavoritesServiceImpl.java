package com.quochuy.store.service.impl;

import com.quochuy.store.mapper.FavoritesMapper;
import com.quochuy.store.model.Favorite;
import com.quochuy.store.record.FavoriteItem;
import com.quochuy.store.service.FavoritesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoritesServiceImpl implements FavoritesService {
	private final FavoritesMapper favoritesMapper;
	
	@Override
	public Optional<String> fetchFavoriteId(UUID userId, UUID productId) {
		return Optional.ofNullable(favoritesMapper.fetchFavoriteId(userId, productId));
	}
	
	@Override
	public List<FavoriteItem> fetchUserFavorites(UUID userId) {
		return favoritesMapper.fetchUserFavorites(userId);
	}
	
	@Override
	public UUID create(UUID productId, UUID userId) {
		Favorite favorite =  Favorite.builder()
				.favoriteId(UUID.randomUUID())
				.createdAt(OffsetDateTime.now())
				.updatedAt(OffsetDateTime.now())
				.userId(userId)
				.productId(productId).build();
		
		favoritesMapper.create(favorite);
		return favorite.getFavoriteId();
	}
	
	@Override
	public void delete(UUID favoriteId, UUID userId) {
		favoritesMapper.delete(favoriteId,userId);
	}
}
