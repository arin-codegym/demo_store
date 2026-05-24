package com.quochuy.chat.conversation.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ConversationParticipant {
	private UUID conversationParticipantId;
	private UUID conversationId;
	private UUID userId;
	private UUID lastReadMessageId;
	private OffsetDateTime joinedAt;
}
