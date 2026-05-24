package com.quochuy.ai.rag.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class RagChunk {
	private String documentId;
	private String chunkId;
	private String sourceTitle;
	private String content;
	private Double score;
}
