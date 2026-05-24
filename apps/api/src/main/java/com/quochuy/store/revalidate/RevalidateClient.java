package com.quochuy.store.revalidate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RevalidateClient {
	@Value("${next.revalidate.products-url}")
	private String productsRevalidateUrl;
	
	@Value("${next.revalidate.secret}")
	private String secret;
	
	private final RestTemplate restTemplate = new RestTemplate();
	
	public void revalidateProducts() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("x-revalidate-secret", secret);
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		HttpEntity<String> entity = new HttpEntity<>("{}", headers);
		
		restTemplate.exchange(
				productsRevalidateUrl,
				HttpMethod.POST,
				entity,
				String.class
		);
	}
}
