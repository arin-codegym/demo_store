package com.quochuy.store.service;

import com.quochuy.store.model.Review;
import com.quochuy.store.record.ProductRating;

import java.util.List;
import java.util.UUID;

public interface ReviewService {
	boolean reviewDoesNotExits(UUID userId, UUID productId);
	
	ProductRating fetchProductRating(UUID productId);
	
	List<Review> fetchProductReviews(UUID productId);
	
	List<Review> fetchProductReviewsByUser(UUID userId);
	
	int deleteReviewByUser(UUID userId, String reviewId);
	
	void createReviewByUser(UUID userId, Review review);
}
