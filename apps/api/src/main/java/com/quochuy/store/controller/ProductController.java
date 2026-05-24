package com.quochuy.store.controller;

import com.quochuy.store.revalidate.RevalidateClient;
import com.quochuy.store.model.Product;
import com.quochuy.security.CustomUserDetails;
import com.quochuy.store.service.impl.ProductServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/backend")
@RequiredArgsConstructor
public class ProductController {
	private final ProductServiceImpl productServiceImpl;
	private final RevalidateClient revalidateClient;
	@GetMapping("/product/featuredProducts")
	@PreAuthorize("permitAll()")
	public ResponseEntity<?> fetchFeaturedProducts() {
		return ResponseEntity.ok(Map.of("featureProducts", productServiceImpl.fetchFeaturedProducts()));
	}
	
	@GetMapping("/product/{productId:[0-9a-fA-F\\\\-]{36}}")
	@PreAuthorize("permitAll()")
	public ResponseEntity<?> fetchSingleProduct(@PathVariable UUID productId) {
		return ResponseEntity.ok(Map.of("product", productServiceImpl.fetchSingleProduct(productId)));
	}
	
//	@GetMapping("/product/fetchAllProducts")
//	@PreAuthorize("permitAll()")
//	public ResponseEntity<?> fetchAllProducts(@RequestParam String search) {
//		return ResponseEntity.ok(
//				Map.of("featureProducts", productService.fetchAllProducts(search)));
//	}
	@GetMapping("/products/search")
	@PreAuthorize("permitAll()")
	public ResponseEntity<?> searchProducts( @RequestParam(defaultValue = "") String search) {
		return ResponseEntity.ok(
				Map.of("products", productServiceImpl.searchProducts(search)));
	}
	@GetMapping("/product/fetchAllProducts")
	@PreAuthorize("permitAll()")
	public ResponseEntity<?> fetchAllProducts() {
		return ResponseEntity.ok(
				Map.of("products", productServiceImpl.fetchAllProducts()));
	}
	
	@GetMapping("/product/fetchAdminProductDetails/{productId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> fetchAdminProductDetails(@PathVariable UUID productId) {
		return ResponseEntity.ok(productServiceImpl.fetchAdminProductDetails(productId));
	}
	
	@GetMapping("/product/updateProductImage/{productId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> updateProductImage(@PathVariable UUID productId,
												@RequestBody String imageUrl) {
		productServiceImpl.updateProductImage(productId, imageUrl);
		revalidateClient.revalidateProducts();
		return ResponseEntity.ok(Map.of("message","Update success"));
	}
	@PostMapping("/product/createProduct")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> createProduct(@RequestBody Product product,
										   @AuthenticationPrincipal CustomUserDetails userDetails) {
		productServiceImpl.createProduct(product, userDetails.getUserId());
		revalidateClient.revalidateProducts();
		return ResponseEntity.ok(Map.of("message","Create success"));
	}
	@PostMapping("/product/updateProduct/{productId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> updateProduct(@PathVariable UUID productId,
												@RequestBody Product product) {
		productServiceImpl.updateProduct(productId, product);
		revalidateClient.revalidateProducts();
		return ResponseEntity.ok(Map.of("message","Update success"));
	}
	@GetMapping("/product/fetchAdminProducts")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> fetchAdminProducts(HttpServletRequest request) {
		System.out.println("ACCEPT = " + request.getHeader("Accept"));
		return ResponseEntity.ok(productServiceImpl.fetchAdminProducts());
	}
	
	@GetMapping("/product/fetchProductUserFavorites")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> fetchProductUserFavorites(HttpServletRequest request,
													   @AuthenticationPrincipal CustomUserDetails customUserDetails) {
		System.out.println("ACCEPT = " + request.getHeader("Accept"));
		return ResponseEntity.ok(productServiceImpl.fetchProductUserFavorites(customUserDetails.getUserId()));
	}
	
	@DeleteMapping("/product/deleteProduct/{productId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> deleteProduct(@PathVariable UUID productId) {
		productServiceImpl.deleteProduct(productId);
		revalidateClient.revalidateProducts();
		return ResponseEntity.noContent().build();
	}
}
