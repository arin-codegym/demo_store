package com.quochuy.ai.product.dto;

import java.util.UUID;

public record InternalProductFact(
		UUID productId,
		String name,
		String company,
		String description,
		String image,
		int price
) {
}
