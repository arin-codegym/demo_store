package com.quochuy.common.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
	private String code;
	private String message;
	private int status;
	private LocalDateTime timestamp;
	private Object errors;
}