package com.quochuy.ai.product.provider;

import com.quochuy.ai.product.dto.ExternalProductContext;
import com.quochuy.ai.product.dto.ExternalProductSearchRequest;
import com.quochuy.helper.AiExternalSearchProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BraveSearchExternalProductSearchProvider implements ExternalProductSearchProvider {
	public static final String PROVIDER_NAME = "brave-search";
	
	private final AiExternalSearchProperties properties;
	
	@Override
	public String providerName() {
		return PROVIDER_NAME;
	}
	
	@Override
	public boolean isConfigured() {
		AiExternalSearchProperties.Brave brave = properties.getBrave();
		return brave != null && !isBlank(brave.getApiKey()) && !isBlank(brave.getEndpoint());
	}
	
	@Override
	public ExternalProductContext search(ExternalProductSearchRequest request) {
		return ExternalProductContext.unavailable(
				providerName(),
				request.query(),
				"Brave Search provider is not implemented yet."
		);
	}
	
	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
