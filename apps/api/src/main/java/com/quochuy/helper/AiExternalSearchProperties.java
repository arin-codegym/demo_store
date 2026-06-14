package com.quochuy.helper;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.external-search")
public class AiExternalSearchProperties {
	private boolean enabled;
	private String provider = "noop";
	private int timeoutMs = 5000;
	private int maxResults = 5;
	private Google google = new Google();
	private Brave brave = new Brave();
	private Tavily tavily = new Tavily();
	
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
	
	public Google getGoogle() {
		return google;
	}
	
	public void setGoogle(Google google) {
		this.google = google;
	}
	
	public Brave getBrave() {
		return brave;
	}
	
	public void setBrave(Brave brave) {
		this.brave = brave;
	}
	
	public Tavily getTavily() {
		return tavily;
	}
	
	public void setTavily(Tavily tavily) {
		this.tavily = tavily;
	}
	
	public static class Google {
		private String endpoint = "https://www.googleapis.com/customsearch/v1";
		private String apiKey;
		private String cx;
		
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
	}
	
	public static class Brave {
		private boolean enabled;
		private String endpoint = "https://api.search.brave.com/res/v1/web/search";
		private String apiKey;
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
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
	}
	
	public static class Tavily {
		private boolean enabled;
		private String endpoint = "https://api.tavily.com/search";
		private String apiKey;
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
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
	}
}
