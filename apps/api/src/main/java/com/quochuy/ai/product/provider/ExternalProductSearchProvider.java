package com.quochuy.ai.product.provider;

import com.quochuy.ai.product.dto.ExternalProductContext;
import com.quochuy.ai.product.dto.ExternalProductSearchRequest;

public interface ExternalProductSearchProvider {
	String providerName();
	
	boolean isConfigured();
	
	ExternalProductContext search(ExternalProductSearchRequest request);
}
