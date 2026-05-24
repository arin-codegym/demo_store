package com.quochuy.ai.chat.listener;

import com.quochuy.ai.chat.service.AiChatService;
import com.quochuy.chat.message.event.MessageCreatedEvent;
import com.quochuy.chat.conversation.enums.ConversationType;
import com.quochuy.chat.conversation.mapper.ConversationMapper;
import com.quochuy.chat.conversation.model.Conversation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Entry point from the chat domain into the AI domain.
 *
 * The listener runs only after the user message transaction commits, so the AI
 * flow never reads a message that might still be rolled back. It also prevents
 * recursive replies by ignoring messages that were created by AI.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class MessageCreatedAiListener {
	
	private final ConversationMapper conversationMapper;
	private final AiChatService aiChatService;
	
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(MessageCreatedEvent event) {
		if (event.isAiMessage()) {
			return;
		}
		
		Conversation conversation = conversationMapper.findById(event.getConversationId());
		if (conversation == null) {
			return;
		}
		
		if (conversation.getType() != ConversationType.USER_AI) {
			return;
		}
		
		// The heavy provider call is delegated to an async service.
		aiChatService.generateReply(event.getConversationId(), event.getMessageId());
	}
}
