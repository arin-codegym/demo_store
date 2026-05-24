package com.quochuy.ai.rag.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RagDocument {
	private Long id;
	private String documentCode;
	private String title;
	private String fileName;
	private String fileExt;
	private String mimeType;
	private Long fileSize;
	private String sourceType;
	private String sourceUri;
	private String checksumSha256;
	private String languageCode;
	private String status;
	private String metadataJson;
	private String rawText;
	private Integer pageCount;
	private LocalDateTime importedAt;
	private String createdBy;
	private LocalDateTime createdAt;
	private String updatedBy;
	private LocalDateTime updatedAt;
	private Boolean deleted;
	private Long importJobId;
	private String assistantCode;
}
