package com.quochuy.common.exception.enums;

import com.quochuy.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum UserErrorCode implements BaseErrorCode {
	
	USER_NOT_FOUND(
			"USER_001",
			HttpStatus.NOT_FOUND,
			"user.not_found"
	),
	
	USERNAME_ALREADY_EXISTS(
			"USER_002",
			HttpStatus.CONFLICT,
			"user.username_already_exists"
	),
	
	EMAIL_ALREADY_EXISTS(
			"USER_003",
			HttpStatus.CONFLICT,
			"user.email_already_exists"
	);
	
	private final String code;
	private final HttpStatus httpStatus;
	private final String messageKey;
	
	UserErrorCode(String code, HttpStatus httpStatus, String messageKey) {
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
