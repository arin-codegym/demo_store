package com.quochuy.ai.product.provider;

import com.quochuy.ai.product.dto.ExternalProductContext;

public interface ExternalProductSearchProvider {
	String providerName();
	
	boolean isConfigured();
	
	ExternalProductContext search(ExternalProductSearchRequest request);
}
