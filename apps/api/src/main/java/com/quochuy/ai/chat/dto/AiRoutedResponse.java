package com.quochuy.ai.chat.dto;

import com.quochuy.ai.chat.enums.AiAnswerSource;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AiRoutedResponse {
	String content;
	AiAnswerSource source;
	String provider;
	String model;
	Integer promptTokens;
	Integer completionTokens;
	Integer totalTokens;
	Long latencyMs;
	boolean cacheable;
}
