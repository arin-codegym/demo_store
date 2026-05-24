package com.quochuy.ai.cache.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class AiAnswerCache {
	private UUID cacheId;
	private String assistantCode;
	private String normalizedQuestion;
	private String answerText;
	private String source; // AI / FAQ
	private Integer hitCount;
	private OffsetDateTime createdAt;
	private OffsetDateTime lastUsedAt;
	private OffsetDateTime expiredAt;
}
