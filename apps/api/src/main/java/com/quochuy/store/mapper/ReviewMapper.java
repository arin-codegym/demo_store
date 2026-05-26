package com.quochuy.store.mapper;

import com.quochuy.store.model.Review;
import com.quochuy.store.record.ProductRating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ReviewMapper {
	boolean reviewDoesNotExits(@Param("userId") UUID userId,
							   @Param("productId") UUID productId);
	
	ProductRating fetchProductRating(@Param("productId") UUID productId);
	
	List<Review> fetchProductReviews(@Param("productId") UUID productId);
	
	List<Review> fetchProductReviewsByUser(@Param("userId") UUID userId);
	
	int deleteReviewByUser(@Param("userId") UUID userId,
						   @Param("reviewId") String reviewId);
	
	int createReviewByUser(Review review);
}
