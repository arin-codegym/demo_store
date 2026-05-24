package com.quochuy.store.service.impl;

import com.quochuy.store.mapper.ReviewMapper;
import com.quochuy.store.model.Review;
import com.quochuy.store.record.ProductRating;
import com.quochuy.store.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
	private final ReviewMapper reviewMapper;
	@Override
	public boolean reviewDoesNotExits(UUID userId, UUID productId) {
		return reviewMapper.reviewDoesNotExits(userId,productId);
	}
	
	public ProductRating fetchProductRating(UUID productId) {
		return Optional.ofNullable(reviewMapper.fetchProductRating(productId))
				.orElseGet(() ->new ProductRating(productId,0,0));
	}
	
	@Override
	public List<Review> fetchProductReviews(UUID productId) {
		return reviewMapper.fetchProductReviews(productId);
	}
	
	@Override
	public List<Review> fetchProductReviewsByUser(UUID userId) {
		return reviewMapper.fetchProductReviewsByUser(userId);
	}
	
	@Override
	public int deleteReviewByUser(UUID userId, String reviewId) {
		return reviewMapper.deleteReviewByUser(userId,reviewId);
	}
	
	@Override
	public void createReviewByUser(UUID userId, Review review) {
		review.setReviewId(UUID.randomUUID());
		review.setUserId(userId);
		reviewMapper.createReviewByUser(review);
	}
}
