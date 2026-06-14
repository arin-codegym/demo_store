package com.quochuy.ai.product.provider;

import com.quochuy.ai.product.dto.ExternalProductContext;
import com.quochuy.helper.AiExternalSearchProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TavilyExternalProductSearchProvider implements ExternalProductSearchProvider {
	public static final String PROVIDER_NAME = "tavily";
	
	private final AiExternalSearchProperties properties;
	
	@Override
	public String providerName() {
		return PROVIDER_NAME;
	}
	
	@Override
	public boolean isConfigured() {
		AiExternalSearchProperties.Tavily tavily = properties.getTavily();
		return tavily != null && tavily.isEnabled() && !isBlank(tavily.getApiKey());
	}
	
	@Override
	public ExternalProductContext search(ExternalProductSearchRequest request) {
		return new ExternalProductContext(
				false,
				providerName(),
				request.query(),
				List.of(),
				"Tavily provider is reserved but not implemented yet."
		);
	}
	
	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
