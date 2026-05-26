package com.quochuy.store.mapper;

import com.quochuy.store.model.Favorite;
import com.quochuy.store.record.FavoriteItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface FavoritesMapper {
	String fetchFavoriteId(@Param("userId") UUID userId,
						   @Param("productId") UUID productId);
	
	List<FavoriteItem> fetchUserFavorites(@Param("userId") UUID userId);
	
	UUID create(Favorite favorite);
	
	void delete(@Param("favoriteId") UUID favoriteId,
				@Param("userId") UUID userId);
}
