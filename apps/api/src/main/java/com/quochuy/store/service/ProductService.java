package com.quochuy.store.service;

import com.quochuy.store.model.Product;

import java.util.List;
import java.util.UUID;

public interface ProductService {
	Product fetchSingleProduct(UUID productId);
	
	List<Product> fetchFeaturedProducts();
	
	List<Product> fetchAllProducts();
	
	Product fetchAdminProductDetails(UUID productId);
	
	int updateProductImage(UUID productId, String imageUrl);
	
	int updateProduct(UUID productId, Product product);
	
	List<Product> fetchAdminProducts();
	
	int deleteProduct(UUID productId);
	
	Product createProduct(Product product,UUID userId);
	
	List<Product> searchProducts(String search);
	
	List<Product> fetchProductUserFavorites(UUID userId);
}
