package com.quochuy.common.exception;

import com.quochuy.common.response.ErrorResponse;
import com.quochuy.common.exception.enums.AuthErrorCode;
import com.quochuy.common.exception.enums.DatabaseErrorCode;
import com.quochuy.common.exception.enums.SystemErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.PersistenceException;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
	
	private final MessageSource messageSource;
	
	private String getMessage(BaseErrorCode errorCode) {
		return messageSource.getMessage(
				errorCode.getMessageKey(),
				null,
				LocaleContextHolder.getLocale()
		);
	}
	
	private ResponseEntity<ErrorResponse> buildResponse(BaseErrorCode errorCode) {
		String message = getMessage(errorCode);
		
		ErrorResponse response = ErrorResponse.builder()
				.code(errorCode.getCode())
				.message(message)
				.status(errorCode.getHttpStatus().value())
				.timestamp(LocalDateTime.now())
				.errors(null)
				.build();
		
		return ResponseEntity
				.status(errorCode.getHttpStatus())
				.contentType(MediaType.APPLICATION_JSON)
				.body(response);
	}
	
	private ResponseEntity<ErrorResponse> buildResponse(
			BaseErrorCode errorCode,
			Object errors
	) {
		String message = getMessage(errorCode);
		
		ErrorResponse response = ErrorResponse.builder()
				.code(errorCode.getCode())
				.message(message)
				.status(errorCode.getHttpStatus().value())
				.timestamp(LocalDateTime.now())
				.errors(errors)
				.build();
		
		return ResponseEntity
				.status(errorCode.getHttpStatus())
				.contentType(MediaType.APPLICATION_JSON)
				.body(response);
	}
	
	@ExceptionHandler(AppException.class)
	public ResponseEntity<ErrorResponse> handleAppException(AppException exception) {
		return buildResponse(exception.getErrorCode());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException exception) {
		log.warn("Access denied: {}", exception.getMessage());
		return buildResponse(AuthErrorCode.ACCESS_DENIED);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(
			MethodArgumentNotValidException exception
	) {
		Map<String, String> errors = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.collect(Collectors.toMap(
						FieldError::getField,
						FieldError::getDefaultMessage,
						(a, b) -> a
				));
		
		return buildResponse(SystemErrorCode.VALIDATION_ERROR, errors);
	}
	
	@ExceptionHandler({
			DataAccessException.class,
			MyBatisSystemException.class,
			PersistenceException.class
	})
	public ResponseEntity<ErrorResponse> handleDatabaseErrors(Exception exception) {
		log.error("Database error", exception);
		
		return buildResponse(DatabaseErrorCode.DATABASE_ERROR);
	}
	
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrity(
			DataIntegrityViolationException exception
	) {
		log.error("Data integrity violation", exception);
		
		return buildResponse(DatabaseErrorCode.DATA_INTEGRITY_VIOLATION);
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(
			IllegalArgumentException exception
	) {
		log.error("Illegal argument: {}", exception.getMessage(), exception);
		
		return buildResponse(SystemErrorCode.ILLEGAL_ARGUMENT);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleAllExceptions(Exception exception) {
		Throwable rootCause = exception;
		
		while (rootCause.getCause() != null) {
			rootCause = rootCause.getCause();
		}
		
		log.error("Unexpected error. Root cause: {}", rootCause.getMessage(), exception);
		
		return buildResponse(SystemErrorCode.INTERNAL_SERVER_ERROR);
	}
}
