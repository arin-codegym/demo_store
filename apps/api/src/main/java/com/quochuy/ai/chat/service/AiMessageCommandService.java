package com.quochuy.ai.chat.service;

import com.quochuy.chat.message.enums.MessageSenderType;
import com.quochuy.chat.message.enums.MessageStatus;
import com.quochuy.chat.conversation.mapper.ConversationMapper;
import com.quochuy.chat.message.mapper.MessageMapper;
import com.quochuy.chat.message.model.Message;
import com.quochuy.chat.message.event.MessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Writes AI-generated messages back into the chat domain.
 *
 * The AI domain uses the same MessageCreatedEvent as user messages so realtime
 * delivery and notifications stay behind the chat event contract.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class AiMessageCommandService {
	
	private final MessageMapper messageMapper;
	private final ConversationMapper conversationMapper;
	private final ApplicationEventPublisher eventPublisher;
	
	@Transactional
	public UUID createAiMessage(UUID conversationId, String content) {
		log.trace("[ai] tx active in createAiMessage = "
								   + TransactionSynchronizationManager.isActualTransactionActive());
		UUID messageId = UUID.randomUUID();
		
		Message message = new Message();
		message.setMessageId(messageId);
		message.setConversationId(conversationId);
		message.setSenderUserId(null);
		message.setSenderType(MessageSenderType.AI);
		message.setClientMessageId(null);
		message.setContent(content);
		message.setStatus(MessageStatus.SENT);
		message.setCreatedAt(OffsetDateTime.now());
		
		messageMapper.insertMessage(message);
		conversationMapper.updateLastMessage(conversationId, message.getMessageId());
		log.debug("[ai] publishing AI MessageCreatedEvent, messageId=" + messageId);
		// Publish after insert so downstream listeners can load the persisted row.
		// Bắn event
		eventPublisher.publishEvent(
				new MessageCreatedEvent(
						messageId,
						conversationId,
						null,
						true
				)
		);
		
		return messageId;
	}
}
