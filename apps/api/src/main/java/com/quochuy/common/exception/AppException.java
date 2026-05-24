package com.quochuy.common.exception;

public class AppException extends RuntimeException{
	private final BaseErrorCode errorCode;
	
	public AppException(BaseErrorCode errorCode) {
		super(errorCode.getMessageKey());
		this.errorCode = errorCode;
	}
	
	public BaseErrorCode getErrorCode() {
		return errorCode;
	}
}
