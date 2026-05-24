package com.quochuy.ai.rag.model;

import lombok.*;

import java.time.LocalDateTime;

@Data

public class RagChunkEntity {
	private Long id;
	private Long documentId;
	private Integer chunkNo;
	private String chunkText;
	private String chunkTextNorm;
	private Integer tokenCount;
	private Integer charCount;
	private Integer pageFrom;
	private Integer pageTo;
	private String sectionTitle;
	private String metadataJson;
	private String embeddingJson;
	private String embeddingVector;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
