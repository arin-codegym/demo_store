package com.quochuy.ai.product.dto;

import java.util.List;

public record ExternalProductContext(
		boolean configured,
		String provider,
		String query,
		List<ExternalProductSearchResult> results,
		String errorMessage
) {
	public boolean hasResults() {
		return results != null && !results.isEmpty();
	}
}
