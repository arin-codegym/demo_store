package com.quochuy.store.mapper;

import com.quochuy.store.model.Review;
import com.quochuy.store.record.ProductRating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ReviewMapper {
	boolean reviewDoesNotExits(@Param("userId") UUID userId, UUID productId);
	
	ProductRating fetchProductRating(UUID productId);
	
	List<Review> fetchProductReviews(UUID productId);
	
	List<Review> fetchProductReviewsByUser(UUID userId);
	
	int deleteReviewByUser(UUID userId, String reviewId);
	
	int createReviewByUser(Review review);
}
