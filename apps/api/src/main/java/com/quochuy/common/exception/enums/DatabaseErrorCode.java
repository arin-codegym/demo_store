package com.quochuy.common.exception.enums;

import com.quochuy.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum DatabaseErrorCode implements BaseErrorCode {
	
	DATABASE_ERROR(
			"DB_001",
			HttpStatus.INTERNAL_SERVER_ERROR,
			"database.query_error"
	),
	
	DATA_INTEGRITY_VIOLATION(
			"DB_002",
			HttpStatus.CONFLICT,
			"database.data_integrity_violation"
	);
	
	private final String code;
	private final HttpStatus httpStatus;
	private final String messageKey;
	
	DatabaseErrorCode(String code, HttpStatus httpStatus, String messageKey) {
		this.code = code;
		this.httpStatus = httpStatus;
		this.messageKey = messageKey;
	}
	
	@Override
	public String getCode() {
		return code;
	}
	
	@Override
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
	
	@Override
	public String getMessageKey() {
		return messageKey;
	}
}
