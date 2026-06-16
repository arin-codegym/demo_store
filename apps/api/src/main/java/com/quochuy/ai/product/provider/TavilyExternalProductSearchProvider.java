package com.quochuy.ai.product.provider;

import com.quochuy.ai.product.dto.ExternalProductContext;
import com.quochuy.ai.product.dto.ExternalProductSearchRequest;
import com.quochuy.helper.AiExternalSearchProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TavilyExternalProductSearchProvider implements ExternalProductSearchProvider {
	public static final String PROVIDER_NAME = "tavily-search";
	
	private final AiExternalSearchProperties properties;
	
	@Override
	public String providerName() {
		return PROVIDER_NAME;
	}
	
	@Override
	public boolean isConfigured() {
		AiExternalSearchProperties.Tavily tavily = properties.getTavily();
		return tavily != null && !isBlank(tavily.getApiKey()) && !isBlank(tavily.getEndpoint());
	}
	
	@Override
	public ExternalProductContext search(ExternalProductSearchRequest request) {
		return ExternalProductContext.unavailable(
				providerName(),
				request.query(),
				"Tavily provider is not implemented yet."
		);
	}
	
	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
