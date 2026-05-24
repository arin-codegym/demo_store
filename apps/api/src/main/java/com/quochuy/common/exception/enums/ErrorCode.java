package com.quochuy.common.exception.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
	
	USER_BANNED(
			"AUTH_001",
			HttpStatus.FORBIDDEN,
			"user.banned"
	),
	
	USER_DELETED(
			"AUTH_002",
			HttpStatus.UNAUTHORIZED,
			"user.deleted"
	),
	
	INVALID_CREDENTIALS(
			"AUTH_003",
			HttpStatus.UNAUTHORIZED,
			"auth.invalid_credentials"
	),
	
	VALIDATION_ERROR(
			"COMMON_001",
			HttpStatus.BAD_REQUEST,
			"validation.invalid_request"
	),
	
	RESOURCE_NOT_FOUND(
			"COMMON_002",
			HttpStatus.NOT_FOUND,
			"resource.not_found"
	),
	
	DATA_INTEGRITY_VIOLATION(
			"DB_001",
			HttpStatus.CONFLICT,
			"database.data_integrity_violation"
	),
	
	DATABASE_ERROR(
			"DB_002",
			HttpStatus.INTERNAL_SERVER_ERROR,
			"database.query_error"
	),
	
	INTERNAL_SERVER_ERROR(
			"SYSTEM_001",
			HttpStatus.INTERNAL_SERVER_ERROR,
			"system.internal_error"
	);
	
	private final String code;
	private final HttpStatus httpStatus;
	private final String messageKey;
	
	ErrorCode(String code, HttpStatus httpStatus, String messageKey) {
		this.code = code;
		this.httpStatus = httpStatus;
		this.messageKey = messageKey;
	}
}