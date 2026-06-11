package com.quochuy.ai.product.dto;

import java.util.List;

public record InternalProductContext(
		List<InternalProductFact> products,
		List<String> attemptedQueries
) {
	public boolean hasProducts() {
		return products != null && !products.isEmpty();
	}
}
