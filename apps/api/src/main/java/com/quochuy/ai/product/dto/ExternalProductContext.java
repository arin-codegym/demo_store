package com.quochuy.ai.product.dto;

import java.util.List;

public record ExternalProductContext(
		boolean configured,
		String provider,
		String query,
		List<ExternalProductSearchResult> results,
		String errorMessage
) {
	public ExternalProductContext {
		provider = provider == null ? "" : provider.trim();
		query = query == null ? "" : query.trim();
		results = results == null ? List.of() : List.copyOf(results);
		errorMessage = errorMessage == null ? null : errorMessage.trim();
	}

	public static ExternalProductContext success(
			String provider,
			String query,
			List<ExternalProductSearchResult> results
	) {
		return new ExternalProductContext(true, provider, query, results, null);
	}

	public static ExternalProductContext unavailable(
			String provider,
			String query,
			String errorMessage
	) {
		return new ExternalProductContext(false, provider, query, List.of(), errorMessage);
	}

	public boolean hasResults() {
		return !results.isEmpty();
	}
}
