package com.quochuy.ai.chat.dto;

import com.quochuy.ai.chat.enums.AiAnswerSource;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiRoutePlan {
	private AiAnswerSource source;
	private String directContent;
	private String systemPrompt;
	private String userPrompt;
	private String provider;
	private String model;
	private Integer promptTokens;
	private Integer completionTokens;
	private Integer totalTokens;
	private Long latencyMs;
	private boolean cacheable;
	private boolean cacheableCandidate;
	
	public boolean isDirect() {
		return directContent != null && !directContent.isBlank();
	}
}
