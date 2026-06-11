package com.quochuy.ai.product;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ProductMentionExtractor {
	private static final Pattern QUOTED_TEXT = Pattern.compile("[\"'“”‘’]([^\"'“”‘’]{2,80})[\"'“”‘’]");
	
	public boolean isProductComparisonQuestion(String question) {
		String q = normalize(question);
		if (q.isBlank()) {
			return false;
		}
		boolean hasCompareIntent = q.contains("so sanh")
				|| q.contains(" compare ")
				|| q.contains(" vs ")
				|| q.contains(" voi ")
				|| q.contains(" khac gi")
				|| q.contains(" tot hon")
				|| q.contains(" nen mua");
		boolean hasProductIntent = q.contains("san pham")
				|| q.contains("hang")
				|| q.contains("cong ty")
				|| q.contains("shop")
				|| q.contains("mua")
				|| q.contains("gia")
				|| q.contains("internet")
				|| q.contains(" vs ");
		return hasCompareIntent && hasProductIntent;
	}
	
	public List<String> extractSearchCandidates(String question) {
		Set<String> candidates = new LinkedHashSet<>();
		String safeQuestion = safe(question);
		Matcher matcher = QUOTED_TEXT.matcher(safeQuestion);
		while (matcher.find()) {
			addCandidate(candidates, matcher.group(1));
		}
		
		String compact = safeQuestion
				.replaceAll("(?i)so\\s+s[aáàảãạăắằẳẵặâấầẩẫậ]nh", " ")
				.replaceAll("(?i)compare", " ")
				.replaceAll("(?i)n[eê]n\\s+mua", " ")
				.replaceAll("(?i)t[oốồổỗộ]t\\s+h[oơ]n", " ");
		
		String[] parts = compact.split("(?i)\\b(vs|với|voi|và|va|hay|or)\\b");
		for (String part : parts) {
			String cleaned = cleanCandidate(part);
			addCandidate(candidates, cleaned);
		}
		
		addCandidate(candidates, cleanCandidate(safeQuestion));
		return new ArrayList<>(candidates);
	}
	
	public String buildExternalQuery(String question, List<String> internalProductNames) {
		String query = safe(question);
		if (internalProductNames != null) {
			for (String name : internalProductNames) {
				if (name != null && !name.isBlank()) {
					query = query.replace(name, " ");
				}
			}
		}
		query = cleanCandidate(query);
		return query.isBlank() ? safe(question) : query;
	}
	
	private void addCandidate(Set<String> candidates, String value) {
		String cleaned = cleanCandidate(value);
		if (cleaned.length() >= 2 && cleaned.length() <= 120) {
			candidates.add(cleaned);
		}
	}
	
	private String cleanCandidate(String value) {
		return safe(value)
				.replaceAll("(?i)\\b(san pham|sản phẩm|cua toi|của tôi|shop|internet|ben ngoai|bên ngoài|cong ty khac|công ty khác)\\b", " ")
				.replaceAll("[?!.:,;()\\[\\]{}]", " ")
				.replaceAll("\\s+", " ")
				.trim();
	}
	
	private String normalize(String value) {
		String s = safe(value).toLowerCase();
		s = Normalizer.normalize(s, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "");
		s = s.replace('đ', 'd');
		s = s.replaceAll("\\s+", " ");
		return " " + s + " ";
	}
	
	private String safe(String value) {
		return value == null ? "" : value.trim();
	}
}
