package com.quochuy.ai.product.dto;

public record ExternalProductResearchRequest(
		String query,
		ExternalSearchIntent intent
) {
	public ExternalProductResearchRequest {
		query = query == null ? "" : query.trim();
		intent = intent == null ? ExternalSearchIntent.PRODUCT_RESEARCH : intent;
	}
}
