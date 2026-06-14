package com.quochuy.ai.product.provider;

import com.quochuy.ai.product.dto.ExternalProductContext;
import com.quochuy.helper.AiExternalSearchProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BraveSearchExternalProductSearchProvider implements ExternalProductSearchProvider {
	public static final String PROVIDER_NAME = "brave";
	
	private final AiExternalSearchProperties properties;
	
	@Override
	public String providerName() {
		return PROVIDER_NAME;
	}
	
	@Override
	public boolean isConfigured() {
		AiExternalSearchProperties.Brave brave = properties.getBrave();
		return brave != null && brave.isEnabled() && !isBlank(brave.getApiKey());
	}
	
	@Override
	public ExternalProductContext search(ExternalProductSearchRequest request) {
		return new ExternalProductContext(
				false,
				providerName(),
				request.query(),
				List.of(),
				"Brave provider is reserved but not implemented yet."
		);
	}
	
	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
