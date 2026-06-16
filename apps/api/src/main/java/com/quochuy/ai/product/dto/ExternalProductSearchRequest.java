package com.quochuy.ai.product.dto;

public record ExternalProductSearchRequest(
		String query,
		int maxResults,
		int timeoutMs,
		ExternalSearchIntent intent
) {
	public ExternalProductSearchRequest {
		query = query == null ? "" : query.trim();
		maxResults = Math.max(1, maxResults);
		timeoutMs = Math.max(1, timeoutMs);
		intent = intent == null ? ExternalSearchIntent.PRODUCT_RESEARCH : intent;
	}
}
