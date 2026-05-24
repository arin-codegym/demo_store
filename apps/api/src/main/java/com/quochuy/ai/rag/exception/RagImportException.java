package com.quochuy.ai.rag.exception;

import lombok.Getter;

@Getter
public class RagImportException extends RuntimeException {
	
	private final String errorCode;
	
	public RagImportException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public RagImportException(String errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}
}
