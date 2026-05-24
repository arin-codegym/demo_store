package com.quochuy.ai.chat.model;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class AiMessageUsage {
	private UUID usageId;
	private UUID conversationId;
	private UUID userMessageId;
	private UUID assistantMessageId;
	private String provider;
	private String model;
	private Integer promptTokens;
	private Integer completionTokens;
	private Integer totalTokens;
	private Long latencyMs;
	private String status;
	private String errorMessage;
	private OffsetDateTime createdAt;
}
