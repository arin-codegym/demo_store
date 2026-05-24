package com.quochuy.chat.message.dto;

import com.quochuy.chat.message.enums.MessageSenderType;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class MessageResponse {
	private UUID messageId;
	private UUID conversationId;
	private UUID senderUserId;
	private MessageSenderType senderType;
	private String clientMessageId;
	private String content;
	private String status;
	private OffsetDateTime createdAt;
}
