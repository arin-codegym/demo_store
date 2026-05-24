package com.quochuy.store.mapper;

import com.quochuy.store.model.Favorite;
import com.quochuy.store.model.Product;
import com.quochuy.store.record.FavoriteItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface FavoritesMapper {
	String fetchFavoriteId( UUID  userId, UUID productId);
	
	List<FavoriteItem> fetchUserFavorites( UUID userId);
	
	void create(UUID productId, UUID userId);
	
	void create(Favorite favorite);
	
	void delete(UUID favoriteId, UUID userId);
}
