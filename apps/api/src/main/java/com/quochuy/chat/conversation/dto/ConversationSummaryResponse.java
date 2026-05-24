package com.quochuy.chat.conversation.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ConversationSummaryResponse {
	private UUID conversationId;
	private UUID  otherUserId;
	private String otherUserName;
	private String otherUserAvatarUrl;
	private UUID  lastMessageId;
	private String lastMessageContent;
	private OffsetDateTime lastMessageAt;
	private Long unreadCount;
}
