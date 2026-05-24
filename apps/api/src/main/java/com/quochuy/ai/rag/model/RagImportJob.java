package com.quochuy.ai.rag.model;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class RagImportJob {
	private Long id;
	private String jobCode;
	private String fileName;
	private String sourceType;
	private String sourceUri;
	private String status;
	private Integer totalDocuments;
	private Integer successDocuments;
	private Integer failedDocuments;
	private String errorMessage;
	private LocalDateTime startedAt;
	private LocalDateTime finishedAt;
	private String createdBy;
	private LocalDateTime createdAt;
}
