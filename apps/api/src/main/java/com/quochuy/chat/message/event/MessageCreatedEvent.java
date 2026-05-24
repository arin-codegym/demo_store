package com.quochuy.chat.message.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class MessageCreatedEvent {
	private final UUID messageId;
	private final UUID conversationId;
	private final UUID senderUserId;
	private boolean aiMessage;
}
