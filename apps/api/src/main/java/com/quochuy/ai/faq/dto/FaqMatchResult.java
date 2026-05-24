package com.quochuy.ai.faq.dto;

import com.quochuy.ai.faq.model.FaqEntry;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FaqMatchResult {
	boolean matched;
	String intentCode;
	String answerText;
	FaqEntry faqEntry;
}
