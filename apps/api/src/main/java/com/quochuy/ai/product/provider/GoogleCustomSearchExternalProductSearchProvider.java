package com.quochuy.ai.product.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quochuy.ai.product.dto.ExternalProductContext;
import com.quochuy.ai.product.dto.ExternalProductSearchResult;
import com.quochuy.helper.AiExternalSearchProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Log4j2
public class GoogleCustomSearchExternalProductSearchProvider
		implements ExternalProductSearchProvider {
	public static final String PROVIDER_NAME = "google-custom-search";
	
	private final AiExternalSearchProperties properties;
	private final ObjectMapper objectMapper;
	
	@Override
	public String providerName() {
		return PROVIDER_NAME;
	}
	
	@Override
	public boolean isConfigured() {
		AiExternalSearchProperties.Google google = properties.getGoogle();
		return google != null
				&& !isBlank(google.getApiKey())
				&& !isBlank(google.getCx())
				&& !isBlank(google.getEndpoint());
	}
	
	@Override
	public ExternalProductContext search(ExternalProductSearchRequest request) {
		String query = safe(request.query());
		try {
			AiExternalSearchProperties.Google google = properties.getGoogle();
			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			requestFactory.setConnectTimeout(request.timeoutMs());
			requestFactory.setReadTimeout(request.timeoutMs());
			RestClient restClient = RestClient.builder()
					.requestFactory(requestFactory)
					.build();
			
			String uri = UriComponentsBuilder.fromUriString(google.getEndpoint())
					.queryParam("key", google.getApiKey())
					.queryParam("cx", google.getCx())
					.queryParam("q", query)
					.queryParam("num", Math.max(1, Math.min(request.maxResults(), 10)))
					.build()
					.toUriString();
			
			String body = restClient.get().uri(uri).retrieve().body(String.class);
			List<ExternalProductSearchResult> results = parseResults(body);
			return new ExternalProductContext(true, providerName(), query, results, null);
		} catch (RestClientException ex) {
			log.warn("Google CSE product search request failed: {}", ex.getMessage());
			return unavailable(query, "Google CSE product search request failed.");
		} catch (Exception ex) {
			log.warn("Google CSE product search parse failed: {}", ex.getMessage());
			return unavailable(query, "Google CSE product search response could not be parsed.");
		}
	}
	
	private List<ExternalProductSearchResult> parseResults(String body) throws Exception {
		if (body == null || body.isBlank()) {
			return List.of();
		}
		JsonNode root = objectMapper.readTree(body);
		JsonNode items = root.path("items");
		if (!items.isArray()) {
			return List.of();
		}
		List<ExternalProductSearchResult> results = new ArrayList<>();
		for (JsonNode item : items) {
			results.add(new ExternalProductSearchResult(
					text(item, "title"),
					text(item, "snippet"),
					text(item, "link"),
					text(item, "displayLink")
			));
		}
		return results;
	}
	
	private ExternalProductContext unavailable(String query, String message) {
		return new ExternalProductContext(false, providerName(), query, List.of(), message);
	}
	
	private String text(JsonNode node, String field) {
		JsonNode value = node.path(field);
		return value.isMissingNode() || value.isNull() ? "" : value.asText("");
	}
	
	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
	
	private String safe(String value) {
		return value == null ? "" : value.trim();
	}
}
