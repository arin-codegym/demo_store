package com.quochuy.common.exception.enums;

import com.quochuy.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum SystemErrorCode implements BaseErrorCode {
	
	VALIDATION_ERROR(
			"COMMON_001",
			HttpStatus.BAD_REQUEST,
			"validation.invalid_request"
	),
	
	INTERNAL_SERVER_ERROR(
			"SYSTEM_001",
			HttpStatus.INTERNAL_SERVER_ERROR,
			"system.internal_error"
	),
	
	ILLEGAL_ARGUMENT(
			"COMMON_002",
			HttpStatus.BAD_REQUEST,
			"common.illegal_argument"
	);
	
	private final String code;
	private final HttpStatus httpStatus;
	private final String messageKey;
	
	SystemErrorCode(String code, HttpStatus httpStatus, String messageKey) {
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
