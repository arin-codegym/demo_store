package com.quochuy.ai.rag.document.dto;

import lombok.Data;

@Data
public class TextChunk {
	private String text;
	private Integer estimatedTokenCount;
}
