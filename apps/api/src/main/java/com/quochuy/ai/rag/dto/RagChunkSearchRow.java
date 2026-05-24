package com.quochuy.ai.rag.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RagChunkSearchRow {
	private String documentId;
	private String chunkId;
	private String sourceTitle;
	private String content;
	private Double score;
}
