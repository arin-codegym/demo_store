package com.quochuy.ai.product.provider;

import com.quochuy.ai.product.dto.ExternalSearchIntent;

public record ExternalProductSearchRequest(
		String query,
		int maxResults,
		int timeoutMs,
		ExternalSearchIntent intent
) {
}
