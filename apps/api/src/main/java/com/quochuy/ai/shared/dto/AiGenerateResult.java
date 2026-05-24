package com.quochuy.ai.shared.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiGenerateResult {
	private String content;
	private String provider;
	private String model;
	private Integer promptTokens;
	private Integer completionTokens;
	private Integer totalTokens;
	private Long latencyMs;
}
