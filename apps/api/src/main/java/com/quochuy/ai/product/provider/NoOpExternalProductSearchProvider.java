package com.quochuy.ai.product.provider;

import com.quochuy.ai.product.dto.ExternalProductContext;
import org.springframework.stereotype.Component;

import java.util.List;

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
		return new ExternalProductContext(
				false,
				providerName(),
				request.query(),
				List.of(),
				"NoOp external product search provider is selected."
		);
	}
}
