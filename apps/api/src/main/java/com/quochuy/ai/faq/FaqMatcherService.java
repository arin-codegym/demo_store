package com.quochuy.ai.faq;

import com.quochuy.ai.faq.mapper.FaqEntryMapper;
import com.quochuy.utils.TextNormalizer;
import com.quochuy.ai.faq.model.FaqEntry;
import com.quochuy.ai.faq.dto.FaqMatchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Deterministic FAQ matcher used before cache, RAG, or LLM calls.
 *
 * FAQ entries are treated as exact normalized substring patterns separated by
 * '|'. This keeps high-confidence support answers cheap and predictable.
 */
@Service
@RequiredArgsConstructor
public class FaqMatcherService  {
	
	private final FaqEntryMapper faqEntryMapper;
	
	public FaqMatchResult match(String rawQuestion) {
		String normalizedQuestion = TextNormalizer.normalizeQuestion(rawQuestion);
		List<FaqEntry> entries = faqEntryMapper.findAllEnabled();
		
		for (FaqEntry entry : entries) {
			if (matches(normalizedQuestion, entry.getQuestionPatterns())) {
				return FaqMatchResult.builder()
						.matched(true)
						.intentCode(entry.getIntentCode())
						.answerText(entry.getAnswerText())
						.faqEntry(entry)
						.build();
			}
		}
		
		return FaqMatchResult.builder()
				.matched(false)
				.intentCode(null)
				.answerText(null)
				.faqEntry(null)
				.build();
	}
	
	private boolean matches(String normalizedQuestion, String patternText) {
		if (patternText == null || patternText.isBlank()) {
			return false;
		}
		
		String[] patterns = patternText.split("\\|");
		for (String pattern : patterns) {
			String normalizedPattern = TextNormalizer.normalizeQuestion(pattern);
			if (!normalizedPattern.isBlank() && normalizedQuestion.contains(normalizedPattern)) {
				return true;
			}
		}
		return false;
	}
}
