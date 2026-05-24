package com.quochuy.store.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {
	private UUID reviewId;
	private int rating;
	private String comment;
	private String authorName;
	private String authorImageUrl;
	// Sử dụng OffsetDateTime cho TIMESTAMPTZ trong Postgres
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
	// 1. Foreign Key (Dùng để thao tác Insert/Update)
	private UUID userId;
	private UUID productId;
	// 2. Navigation Property (Dùng để chứa kết quả JOIN khi Select)
	private Product product;
}
