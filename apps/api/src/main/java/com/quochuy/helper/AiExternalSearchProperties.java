package com.quochuy.helper;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.external-search")
public class AiExternalSearchProperties {
	private boolean enabled;
	private String provider = "google-custom-search";
	private String endpoint = "https://www.googleapis.com/customsearch/v1";
	private String apiKey;
	private String cx;
	private int timeoutMs = 5000;
	private int maxResults = 5;
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public String getProvider() {
		return provider;
	}
	
	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	public String getEndpoint() {
		return endpoint;
	}
	
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	
	public String getApiKey() {
		return apiKey;
	}
	
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public String getCx() {
		return cx;
	}
	
	public void setCx(String cx) {
		this.cx = cx;
	}
	
	public int getTimeoutMs() {
		return timeoutMs;
	}
	
	public void setTimeoutMs(int timeoutMs) {
		this.timeoutMs = timeoutMs;
	}
	
	public int getMaxResults() {
		return maxResults;
	}
	
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}
}
