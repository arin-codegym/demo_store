package com.quochuy.store.model;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;
@Data
@Builder
public class Favorite {
	private UUID favoriteId ;
	private OffsetDateTime createdAt ;
	private OffsetDateTime updatedAt ;
	private UUID userId ;
	private UUID productId ;
}
