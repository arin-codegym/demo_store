package com.quochuy.chat.message.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ConversationSeenEvent {
	private final UUID conversationId;
	private final UUID userId;
	private final UUID lastReadMessageId;
}
