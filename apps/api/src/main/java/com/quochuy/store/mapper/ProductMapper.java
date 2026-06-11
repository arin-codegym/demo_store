package com.quochuy.store.mapper;

import com.quochuy.store.model.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ProductMapper {
	Product fetchSingleProduct(UUID productId);
	
	List<Product> fetchFeaturedProducts();
	
	List<Product> fetchAllProducts();
	
	int updateProductImage(UUID productId, String imageUrl);
	
	int updateProduct(UUID productId, Product product);
	
	List<Product> fetchAdminProducts();
	
	int deleteProduct(UUID productId);
	
	Product createProduct(Product product);
	
	List<Product> searchProducts(String search);
	
	List<Product> searchTopProductsForAi(@Param("query") String query, @Param("limit") int limit);
	
	List<Product> fetchProductUserFavorites(UUID userId);
}
