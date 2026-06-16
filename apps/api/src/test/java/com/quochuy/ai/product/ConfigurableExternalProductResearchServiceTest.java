package com.quochuy.ai.product;

import com.quochuy.ai.product.dto.ExternalProductContext;
import com.quochuy.ai.product.dto.ExternalProductResearchRequest;
import com.quochuy.ai.product.dto.ExternalProductSearchRequest;
import com.quochuy.ai.product.dto.ExternalSearchIntent;
import com.quochuy.ai.product.provider.ExternalProductSearchProvider;
import com.quochuy.helper.AiExternalSearchProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurableExternalProductResearchServiceTest {
	
	@Test
	void returnsUnavailableWhenExternalSearchIsDisabled() {
		AiExternalSearchProperties properties = properties(false, "mock-provider");
		FakeProvider provider = new FakeProvider("mock-provider", true);
		ConfigurableExternalProductResearchService service = service(properties, provider);
		
		ExternalProductContext result = service.search(new ExternalProductResearchRequest(
				"iphone 15",
				ExternalSearchIntent.PRICE_CHECK
		));
		
		assertFalse(result.configured());
		assertEquals("mock-provider", result.provider());
		assertEquals("iphone 15", result.query());
		assertEquals(0, provider.calls);
	}
	
	@Test
	void returnsUnavailableWhenProviderIsUnknown() {
		AiExternalSearchProperties properties = properties(true, "missing-provider");
		ConfigurableExternalProductResearchService service = service(properties);
		
		ExternalProductContext result = service.search(new ExternalProductResearchRequest(
				"sony xm5",
				ExternalSearchIntent.PRODUCT_RESEARCH
		));
		
		assertFalse(result.configured());
		assertEquals("missing-provider", result.provider());
		assertTrue(result.errorMessage().contains("not registered"));
	}
	
	@Test
	void returnsUnavailableWhenProviderIsNotConfigured() {
		AiExternalSearchProperties properties = properties(true, "mock-provider");
		FakeProvider provider = new FakeProvider("mock-provider", false);
		ConfigurableExternalProductResearchService service = service(properties, provider);
		
		ExternalProductContext result = service.search(new ExternalProductResearchRequest(
				"macbook air",
				ExternalSearchIntent.PRODUCT_COMPARISON
		));
		
		assertFalse(result.configured());
		assertEquals("mock-provider", result.provider());
		assertTrue(result.errorMessage().contains("not configured"));
		assertEquals(0, provider.calls);
	}
	
	@Test
	void routesToConfiguredProviderWithIntentAndSearchOptions() {
		AiExternalSearchProperties properties = properties(true, "mock-provider");
		properties.setMaxResults(7);
		properties.setTimeoutMs(2500);
		FakeProvider provider = new FakeProvider("mock-provider", true);
		ConfigurableExternalProductResearchService service = service(properties, provider);
		
		ExternalProductContext result = service.search(new ExternalProductResearchRequest(
				"galaxy s26",
				ExternalSearchIntent.PRICE_CHECK
		));
		
		assertTrue(result.configured());
		assertEquals("mock-provider", result.provider());
		assertEquals("galaxy s26", result.query());
		assertEquals(1, provider.calls);
		assertEquals("galaxy s26", provider.lastRequest.query());
		assertEquals(7, provider.lastRequest.maxResults());
		assertEquals(2500, provider.lastRequest.timeoutMs());
		assertSame(ExternalSearchIntent.PRICE_CHECK, provider.lastRequest.intent());
	}
	
	private ConfigurableExternalProductResearchService service(
			AiExternalSearchProperties properties,
			ExternalProductSearchProvider... providers
	) {
		return new ConfigurableExternalProductResearchService(properties, List.of(providers));
	}
	
	private AiExternalSearchProperties properties(boolean enabled, String provider) {
		AiExternalSearchProperties properties = new AiExternalSearchProperties();
		properties.setEnabled(enabled);
		properties.setProvider(provider);
		return properties;
	}
	
	private static class FakeProvider implements ExternalProductSearchProvider {
		private final String providerName;
		private final boolean configured;
		private int calls;
		private ExternalProductSearchRequest lastRequest;
		
		private FakeProvider(String providerName, boolean configured) {
			this.providerName = providerName;
			this.configured = configured;
		}
		
		@Override
		public String providerName() {
			return providerName;
		}
		
		@Override
		public boolean isConfigured() {
			return configured;
		}
		
		@Override
		public ExternalProductContext search(ExternalProductSearchRequest request) {
			calls++;
			lastRequest = request;
			return ExternalProductContext.success(providerName(), request.query(), List.of());
		}
	}
}
