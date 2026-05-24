package com.quochuy.store.service;

import com.quochuy.store.record.FavoriteItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoritesService {
	Optional<String> fetchFavoriteId(UUID userId, UUID productId);
	
	List<FavoriteItem> fetchUserFavorites(UUID userId);
	
	UUID create(UUID productId, UUID userId);
	
	void delete(UUID productId, UUID userId);
}
