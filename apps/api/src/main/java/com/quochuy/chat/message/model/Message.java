package com.quochuy.chat.message.model;

import com.quochuy.chat.message.enums.MessageSenderType;
import com.quochuy.chat.message.enums.MessageStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class Message {
	private UUID messageId;
	private UUID conversationId;
	private UUID senderUserId;// null nếu là AI
	private MessageSenderType senderType; // USER | AI
	private String clientMessageId;
	private String content;
	private MessageStatus status; // SENT / FAILED
	private OffsetDateTime createdAt;
	private boolean inserted;
}
