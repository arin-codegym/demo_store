package com.quochuy.chat.conversation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateDirectConversationRequest {
	@NotNull
	private UUID targetUserId;
}
