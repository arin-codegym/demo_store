package com.quochuy.ai.faq.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class FaqEntry {
	private UUID faqId;
	private String intentCode;
	private String questionPatterns; // ví dụ: "dia chi|o dau|cua hang o dau"
	private String answerText;
	private Boolean enabled;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
}
