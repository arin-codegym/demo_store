package com.quochuy.common.exception.enums;

import com.quochuy.common.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements BaseErrorCode {
	USER_BANNED("AUTH_001", HttpStatus.FORBIDDEN, "auth.user_banned"),
	USER_DELETED("AUTH_002",HttpStatus.UNAUTHORIZED,"auth.user_deleted"),
	INVALID_CREDENTIALS("AUTH_003", HttpStatus.UNAUTHORIZED, "auth.invalid_credentials"),
	EMAIL_NOT_VERIFIED("AUTH_004", HttpStatus.FORBIDDEN, "auth.email_not_verified"),
	ACCESS_DENIED("AUTH_005", HttpStatus.FORBIDDEN, "auth.access_denied");
	private final String code;
	private final HttpStatus httpStatus;
	private final String messageKey;
	
	AuthErrorCode(String code, HttpStatus httpStatus, String messageKey) {
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
