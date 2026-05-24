package com.quochuy.ai.cache.service;

import com.quochuy.ai.cache.mapper.AiAnswerCacheMapper;
import com.quochuy.utils.TextNormalizer;
import com.quochuy.ai.cache.model.AiAnswerCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Stores and reuses safe generic AI answers.
 *
 * The cache is intentionally conservative: questions or answers that look
 * personal, account-related, or order-related are not cached.
 */
@Service
@RequiredArgsConstructor
public class AiAnswerCacheService {
	
	private final AiAnswerCacheMapper aiAnswerCacheMapper;
	
	private static final Pattern PERSONAL_DATA_PATTERN =
			Pattern.compile("\\b(order|don hang|email|sdt|so dien thoai|ma don|user id|tai khoan)\\b",
							Pattern.CASE_INSENSITIVE);
	
	public AiAnswerCache findReusableAnswer(String assistantCode, String rawQuestion) {
		String normalizedQuestion = TextNormalizer.normalizeQuestion(rawQuestion);
		AiAnswerCache cache = aiAnswerCacheMapper.findReusableAnswer(assistantCode, normalizedQuestion);
		
		if (cache != null) {
			aiAnswerCacheMapper.increaseHitCount(cache.getCacheId(), OffsetDateTime.now());
		}
		
		return cache;
	}
	
	public void saveReusableAnswer(String assistantCode, String rawQuestion, String answerText, String source) {
		String normalizedQuestion = TextNormalizer.normalizeQuestion(rawQuestion);
		
		AiAnswerCache cache = new AiAnswerCache();
		cache.setCacheId(UUID.randomUUID());
		cache.setAssistantCode(assistantCode);
		cache.setNormalizedQuestion(normalizedQuestion);
		cache.setAnswerText(answerText);
		cache.setSource(source);
		cache.setHitCount(0);
		cache.setCreatedAt(OffsetDateTime.now());
		cache.setLastUsedAt(OffsetDateTime.now());
		cache.setExpiredAt(null);
		
		aiAnswerCacheMapper.insert(cache);
	}
	
	public boolean shouldCache(String rawQuestion, String answerText) {
		if (rawQuestion == null || rawQuestion.isBlank()) {
			return false;
		}
		
		if (answerText == null || answerText.isBlank()) {
			return false;
		}
		
		String normalizedQuestion = TextNormalizer.normalizeQuestion(rawQuestion);
		String normalizedAnswer = TextNormalizer.normalizeQuestion(answerText);
		
		if (normalizedQuestion.length() < 5) {
			return false;
		}
		
		if (PERSONAL_DATA_PATTERN.matcher(normalizedQuestion).find()) {
			return false;
		}
		
		if (PERSONAL_DATA_PATTERN.matcher(normalizedAnswer).find()) {
			return false;
		}
		
		return normalizedAnswer.length() <= 1000;
	}
}
