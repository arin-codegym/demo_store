package com.quochuy.ai.product.provider;

import com.quochuy.ai.product.dto.ExternalProductContext;
import com.quochuy.ai.product.dto.ExternalProductSearchRequest;
import org.springframework.stereotype.Component;

@Component
public class NoOpExternalProductSearchProvider implements ExternalProductSearchProvider {
	public static final String PROVIDER_NAME = "noop";
	
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
		return ExternalProductContext.unavailable(
				providerName(),
				request.query(),
				"NoOp external product search provider is selected."
		);
	}
}
