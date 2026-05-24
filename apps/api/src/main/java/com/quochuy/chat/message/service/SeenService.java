package com.quochuy.chat.message.service;

import com.quochuy.chat.conversation.service.ConversationService;
import com.quochuy.chat.conversation.mapper.ConversationParticipantMapper;
import com.quochuy.chat.message.event.ConversationSeenEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeenService {
	
	private final ConversationService conversationService;
	private final ConversationParticipantMapper conversationParticipantMapper;
	private final ApplicationEventPublisher eventPublisher;
	
	@Transactional
	public void markSeen(UUID currentUserId, UUID conversationId, UUID lastReadMessageId) {
		conversationService.validateParticipant(conversationId, currentUserId);
		
		conversationParticipantMapper.updateLastReadMessageId(conversationId, currentUserId, lastReadMessageId);
		
		eventPublisher.publishEvent(
				new ConversationSeenEvent(conversationId, currentUserId, lastReadMessageId)
		);
	}
}

