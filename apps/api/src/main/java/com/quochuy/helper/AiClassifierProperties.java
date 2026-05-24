package com.quochuy.helper;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "ai.classifier")
public class AiClassifierProperties {
	private List<String> storeInfoKeywords;
	private List<String> orderStatusKeywords;
	private List<String> productInfoKeywords;
	
	public List<String> getStoreInfoKeywords() {
		return storeInfoKeywords;
	}
	
	public void setStoreInfoKeywords(List<String> storeInfoKeywords) {
		this.storeInfoKeywords = storeInfoKeywords;
	}
	
	public List<String> getOrderStatusKeywords() {
		return orderStatusKeywords;
	}
	
	public void setOrderStatusKeywords(List<String> orderStatusKeywords) {
		this.orderStatusKeywords = orderStatusKeywords;
	}
	
	public List<String> getProductInfoKeywords() {
		return productInfoKeywords;
	}
	
	public void setProductInfoKeywords(List<String> productInfoKeywords) {
		this.productInfoKeywords = productInfoKeywords;
	}
}
