package com.quochuy.store.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
	
	private UUID orderItemId;
	
	private UUID orderId;
	
	private UUID productId;
	
	private String productName;
	
	private String productImage;
	
	private Integer unitPrice;
	
	private Integer amount;
	
	private Integer lineTotal;
	
	private OffsetDateTime createdAt;
}
