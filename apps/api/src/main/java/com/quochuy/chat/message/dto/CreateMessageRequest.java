package com.quochuy.chat.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;
@Data
public class CreateMessageRequest {
	@NotNull
	private UUID conversationId;
	
	@NotBlank
	private String content;
}
