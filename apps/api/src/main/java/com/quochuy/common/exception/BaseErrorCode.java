package com.quochuy.common.exception;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {
	String getCode();
	HttpStatus getHttpStatus();
	String getMessageKey();
}
