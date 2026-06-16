package com.quochuy.ai.product.provider;

import com.quochuy.ai.product.dto.ExternalProductContext;
import com.quochuy.ai.product.dto.ExternalProductSearchResult;
import com.quochuy.ai.product.dto.ExternalProductSearchRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MockExternalProductSearchProvider implements ExternalProductSearchProvider {
	public static final String PROVIDER_NAME = "mock";
	
	@Override
	public String providerName() {
		return PROVIDER_NAME;
	}
	
	@Override
	public boolean isConfigured() {
		return true;
	}
	
	@Override
	public ExternalProductContext search(ExternalProductSearchRequest request) {
		return ExternalProductContext.success(
				providerName(),
				request.query(),
				List.of(new ExternalProductSearchResult(
						"Mock external product result",
						"Mock result for development only. Configure a real provider for live web data.",
						"mock://external-product-search",
						"mock"
				))
		);
	}
}
