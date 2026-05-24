package com.quochuy.store.service.impl;

import com.quochuy.common.exception.ResourceNotFoundException;
import com.quochuy.store.mapper.ProductMapper;
import com.quochuy.store.model.Product;
import com.quochuy.store.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
	private final ProductMapper productMapper;
	@Override
	public Product fetchSingleProduct(UUID productId) {
		return productMapper.fetchSingleProduct(productId);
	}
	
	@Override
	public List<Product> fetchFeaturedProducts() {
		return productMapper.fetchFeaturedProducts();
	}
	
	@Override
	public List<Product> fetchAllProducts() {
		return productMapper.fetchAllProducts();
	}
	
	@Override
	public Product fetchAdminProductDetails(UUID productId) {
		return Optional.ofNullable(productMapper.fetchSingleProduct(productId))
				.orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm id: " + productId));
	}
	
	@Override
	public int updateProductImage(UUID productId, String imageUrl) {
		return productMapper.updateProductImage(productId,imageUrl);
	}
	
	@Override
	public int updateProduct(UUID productId, Product product) {
		return productMapper.updateProduct( productId,  product);
	}
	
	@Override
	public List<Product> fetchAdminProducts() {
		return productMapper.fetchAdminProducts();
	}
	
	@Override
	public int deleteProduct(UUID productId) {
		return productMapper.deleteProduct(productId);
	}
	
	@Override
	public Product createProduct(Product product, UUID userId) {
		product.setProductId(UUID.randomUUID());
		product.setUserId(userId);
		return productMapper.createProduct(product);
	}
	
	@Override
	public List<Product> searchProducts(String search) {
		return productMapper.searchProducts(search);
	}
	
	@Override
	public List<Product> fetchProductUserFavorites(UUID userId) {
		return productMapper.fetchProductUserFavorites(userId);
	}
}
