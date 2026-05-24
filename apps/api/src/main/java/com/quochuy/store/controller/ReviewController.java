package com.quochuy.store.controller;

import com.quochuy.store.model.Review;
import com.quochuy.security.CustomUserDetails;
import com.quochuy.store.service.impl.ReviewServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/api/backend/review")
@RequiredArgsConstructor
public class ReviewController {
	private final ReviewServiceImpl reviewServiceImpl;
	
	@GetMapping("/review-does-not-exist/{productId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> findExistingReview(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable UUID productId) {
		/* Nhờ @PreAuthorize, ta không cần: if (userDetails == null) { ... }*/
		//		if (userDetails == null) {
		//			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		//		}
		// Bây giờ bạn có thể gọi getUserId() trực tiếp vì đã khai báo đúng kiểu CustomUserDetails
		UUID userId = userDetails.getUserId();
		boolean check = reviewServiceImpl.reviewDoesNotExits(userId, productId);
		return ResponseEntity.ok(Map.of("exits", check));
	}
	
	@GetMapping("/fetchProductRating/{productId}")
	public ResponseEntity<?> fetchProductRating( @PathVariable UUID productId) {
		return ResponseEntity.ok(reviewServiceImpl.fetchProductRating(productId));
	}
	
	@GetMapping("/fetchProductReviews/{productId}")
	public ResponseEntity<?> fetchProductReviews( @PathVariable UUID productId) {
		return ResponseEntity.ok(reviewServiceImpl.fetchProductReviews(productId));
	}
	
	@GetMapping("/fetchProductReviewsByUser/")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> fetchProductReviewsByUser( @AuthenticationPrincipal CustomUserDetails customUserDetails) {
		return ResponseEntity.ok(reviewServiceImpl.fetchProductReviewsByUser(customUserDetails.getUserId()));
	}
	
	@DeleteMapping("/deleteReviewByUser/{reviewId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> deleteReviewByUser(
			@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@PathVariable String reviewId) {
		return ResponseEntity.ok(
				reviewServiceImpl.deleteReviewByUser(customUserDetails.getUserId(), reviewId));
	}
	
	@PostMapping("/createReview")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> createReviewByUser(
			@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@RequestBody Review review) {
		reviewServiceImpl.createReviewByUser(customUserDetails.getUserId(), review);
		return ResponseEntity.ok(Map.of("message","success"));
	}
}
