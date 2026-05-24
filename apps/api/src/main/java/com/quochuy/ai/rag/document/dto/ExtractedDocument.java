package com.quochuy.ai.rag.document.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ExtractedDocument {
	private final String text;
	private final String extractorType;
	private final Integer pageCount;
	private final String metadataJson;
}
