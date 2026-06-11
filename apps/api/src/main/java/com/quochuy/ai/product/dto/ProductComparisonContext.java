package com.quochuy.ai.product.dto;

public record ProductComparisonContext(
		String question,
		InternalProductContext internalProduct,
		ExternalProductContext externalProduct
) {
}
