package com.quochuy.ai.business;

import com.quochuy.helper.AiClassifierProperties;
import com.quochuy.ai.business.enums.AiQuestionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

/**
 * Lightweight rule classifier for business-context questions.
 *
 * It only selects a broad question type. The actual facts are resolved by
 * AiBusinessContextResolver.
 */
@Service
@RequiredArgsConstructor
public class AiQuestionClassifier  {
	private final AiClassifierProperties properties;
	public AiQuestionType classify(String question) {
		String q = normalize(question);
		
		if (containsAny(q, properties.getStoreInfoKeywords())) {
			return AiQuestionType.STORE_INFO;
		}
		
		if (containsAny(q, properties.getOrderStatusKeywords())) {
			return AiQuestionType.ORDER_STATUS;
		}
		
		if (containsAny(q, properties.getProductInfoKeywords())) {
			return AiQuestionType.PRODUCT_INFO;
		}
		
		return AiQuestionType.GENERAL;
	}
	
	private String normalize(String value) {
		if (value == null) return "";
		
		String s = value.trim().toLowerCase();
		s = Normalizer.normalize(s, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "");   // bỏ dấu
		s = s.replace('đ', 'd');
		s = s.replaceAll("\\s+", " ");
		return s;
	}
	private boolean containsAny(String text, List<String> keywords) {
		if (keywords == null || keywords.isEmpty()) return false;
		return keywords.stream().anyMatch(text::contains);
	}
}

