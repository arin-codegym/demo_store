package com.quochuy.ai.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quochuy.ai.product.dto.ExternalProductContext;
import com.quochuy.ai.product.dto.ExternalProductSearchResult;
import com.quochuy.helper.AiExternalSearchProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class GoogleCustomSearchExternalProductResearchService
		implements ExternalProductResearchService {
	private final AiExternalSearchProperties properties;
	private final ObjectMapper objectMapper;
	
	@Override
	public ExternalProductContext search(String query) {
		String normalizedQuery = safe(query);
		if (!properties.isEnabled()) {
			return unavailable(normalizedQuery, "External product search is disabled.");
		}
		if (isBlank(properties.getApiKey()) || isBlank(properties.getCx())) {
			return unavailable(normalizedQuery, "External product search API key/cx is not configured.");
		}
		if (normalizedQuery.isBlank()) {
			return unavailable(normalizedQuery, "External product search query is blank.");
		}
		
		try {
			SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
			requestFactory.setConnectTimeout(properties.getTimeoutMs());
			requestFactory.setReadTimeout(properties.getTimeoutMs());
			RestClient restClient = RestClient.builder()
					.requestFactory(requestFactory)
					.build();
			
			String uri = UriComponentsBuilder.fromUriString(properties.getEndpoint())
					.queryParam("key", properties.getApiKey())
					.queryParam("cx", properties.getCx())
					.queryParam("q", normalizedQuery)
					.queryParam("num", Math.max(1, Math.min(properties.getMaxResults(), 10)))
					.build()
					.toUriString();
			
			String body = restClient.get().uri(uri).retrieve().body(String.class);
			List<ExternalProductSearchResult> results = parseResults(body);
			return new ExternalProductContext(true, normalizedQuery, results, null);
		} catch (RestClientException ex) {
			log.warn("External product search request failed: {}", ex.getMessage());
			return unavailable(normalizedQuery, "External product search request failed.");
		} catch (Exception ex) {
			log.warn("External product search parse failed: {}", ex.getMessage());
			return unavailable(normalizedQuery, "External product search response could not be parsed.");
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
		return new ExternalProductContext(false, query, List.of(), message);
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
