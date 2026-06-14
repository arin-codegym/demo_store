package com.quochuy.ai.product;

import com.quochuy.ai.product.dto.ExternalProductContext;
import com.quochuy.ai.product.dto.ExternalSearchIntent;
import com.quochuy.ai.product.provider.ExternalProductSearchProvider;
import com.quochuy.ai.product.provider.ExternalProductSearchRequest;
import com.quochuy.helper.AiExternalSearchProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Log4j2
public class ConfigurableExternalProductResearchService implements ExternalProductResearchService {
	private final AiExternalSearchProperties properties;
	private final Map<String, ExternalProductSearchProvider> providers;
	
	public ConfigurableExternalProductResearchService(
			AiExternalSearchProperties properties,
			List<ExternalProductSearchProvider> providers
	) {
		this.properties = properties;
		this.providers = providers.stream()
				.collect(Collectors.toUnmodifiableMap(
						provider -> normalize(provider.providerName()),
						Function.identity()
				));
	}
	
	@Override
	public ExternalProductContext search(String query) {
		String normalizedQuery = safe(query);
		String providerName = normalize(properties.getProvider());
		
		if (!properties.isEnabled()) {
			return unavailable(providerName, normalizedQuery, "External product search is disabled.");
		}
		if (normalizedQuery.isBlank()) {
			return unavailable(providerName, normalizedQuery, "External product search query is blank.");
		}
		
		ExternalProductSearchProvider provider = providers.get(providerName);
		if (provider == null) {
			log.warn("External product search provider is not registered: {}", providerName);
			return unavailable(providerName, normalizedQuery,
							   "External product search provider is not registered: " + providerName);
		}
		if (!provider.isConfigured()) {
			return unavailable(providerName, normalizedQuery,
							   "External product search provider is not configured: " + providerName);
		}
		
		return provider.search(new ExternalProductSearchRequest(
				normalizedQuery,
				properties.getMaxResults(),
				properties.getTimeoutMs(),
				ExternalSearchIntent.PRODUCT_COMPARISON
		));
	}
	
	private ExternalProductContext unavailable(String provider, String query, String message) {
		return new ExternalProductContext(false, provider, query, List.of(), message);
	}
	
	private String normalize(String value) {
		return safe(value).toLowerCase(Locale.ROOT);
	}
	
	private String safe(String value) {
		return value == null ? "" : value.trim();
	}
}
