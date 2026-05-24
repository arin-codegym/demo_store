package com.quochuy.chat.message.dto;

import lombok.Data;

import java.util.UUID;
@Data
public class MarkReadByConversationRequest {
	private UUID conversationId;
}
