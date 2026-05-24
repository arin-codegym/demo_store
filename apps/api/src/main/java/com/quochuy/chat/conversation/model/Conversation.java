package com.quochuy.chat.conversation.model;

import com.quochuy.chat.conversation.enums.ConversationType;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class Conversation {
	private UUID conversationId;
	private ConversationType type; // DIRECT
	private String assistantCode;
	private UUID lastMessageId;
	private OffsetDateTime lastMessageAt;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
}
