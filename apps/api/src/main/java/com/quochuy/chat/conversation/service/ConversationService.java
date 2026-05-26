package com.quochuy.chat.conversation.service;

import com.quochuy.chat.conversation.enums.ConversationType;
import com.quochuy.chat.conversation.mapper.AiConversationPairMapper;
import com.quochuy.chat.conversation.mapper.ConversationMapper;
import com.quochuy.chat.conversation.mapper.ConversationParticipantMapper;
import com.quochuy.chat.conversation.mapper.DirectConversationPairMapper;
import com.quochuy.store.mapper.UserMapper;
import com.quochuy.chat.conversation.dto.ConversationSummaryResponse;
import com.quochuy.chat.conversation.model.Conversation;
import com.quochuy.chat.conversation.model.ConversationParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {
	private final ConversationMapper conversationMapper;
	private final ConversationParticipantMapper conversationParticipantMapper;
	private final DirectConversationPairMapper directConversationPairMapper;
	private final AiConversationPairMapper aiConversationPairMapper;
	private final UserMapper userMapper;
	
	@Transactional
	public UUID createOrGetDirectConversation(UUID currentUserId, UUID targetUserId) {
		return createOrGetDirectConversationInternal(currentUserId, targetUserId);
	}
	
	@Transactional
	public UUID createOrGetAdminConversation(UUID userId) {
		UUID adminId = userMapper.getAdminId();
		return createOrGetDirectConversationInternal(userId, adminId);
	}
	
	private UUID createOrGetDirectConversationInternal(UUID currentUserId, UUID targetUserId) {
		if (currentUserId == null || targetUserId == null) {
			throw new IllegalArgumentException("User id must not be null");
		}
		
		if (currentUserId.equals(targetUserId)) {
			throw new IllegalArgumentException("Cannot create direct conversation with yourself");
		}
		
		if (userMapper.findById(targetUserId) == null) {
			throw new IllegalArgumentException("Target user does not exist");
		}
		
		UUID user1 = normalizeUser1(currentUserId, targetUserId);
		UUID user2 = normalizeUser2(currentUserId, targetUserId);
		OffsetDateTime time = OffsetDateTime.now();
		Conversation conversation = new Conversation();
		conversation.setType(ConversationType.USER_DIRECT);
		conversation.setAssistantCode(null);
		conversation.setCreatedAt(time);
		conversation.setUpdatedAt(time);
		conversationMapper.insertConversation(conversation);
		
		UUID conversationId = directConversationPairMapper.upsertPair(
				conversation.getConversationId(),
				user1,
				user2
		);
		
		if (!conversation.getConversationId().equals(conversationId)) {
			conversationMapper.deleteById(conversation.getConversationId());
			return conversationId;
		}
		
		ConversationParticipant p1 = new ConversationParticipant();
		p1.setConversationId(conversationId);
		p1.setUserId(currentUserId);
		p1.setJoinedAt(time);
		ConversationParticipant p2 = new ConversationParticipant();
		p2.setConversationId(conversationId);
		p2.setUserId(targetUserId);
		p2.setJoinedAt(time);
		conversationParticipantMapper.insertParticipant(p1);
		conversationParticipantMapper.insertParticipant(p2);
		return conversationId;
	}
	
	@Transactional
	public UUID createOrGetAiConversation(UUID currentUserId, String assistantCode) {
		if (currentUserId == null) {
			throw new IllegalArgumentException("User id must not be null");
		}
		
		String normalizedAssistantCode = assistantCode == null ? null : assistantCode.trim();
		if (normalizedAssistantCode == null || normalizedAssistantCode.isBlank()) {
			throw new IllegalArgumentException("Assistant code must not be blank");
		}
		
		if (userMapper.findById(currentUserId) == null) {
			throw new IllegalArgumentException("User does not exist");
		}
		
		UUID existedConversationId =
				conversationMapper.findAiConversationIdByUserIdAndAssistantCode(
						currentUserId,
						normalizedAssistantCode
				);
		
		if (existedConversationId != null) {
			return aiConversationPairMapper.upsertPair(
					existedConversationId,
					currentUserId,
					normalizedAssistantCode
			);
		}
		OffsetDateTime time = OffsetDateTime.now();
		Conversation conversation = new Conversation();
		conversation.setType(ConversationType.USER_AI);
		conversation.setAssistantCode(normalizedAssistantCode);
		conversation.setCreatedAt(time);
		conversation.setUpdatedAt(time);
		conversationMapper.insertConversation(conversation);
		
		UUID conversationId = aiConversationPairMapper.upsertPair(
				conversation.getConversationId(),
				currentUserId,
				normalizedAssistantCode
		);
		
		if (!conversation.getConversationId().equals(conversationId)) {
			conversationMapper.deleteById(conversation.getConversationId());
			return conversationId;
		}
		
		ConversationParticipant participant = new ConversationParticipant();
		participant.setConversationId(conversationId);
		participant.setUserId(currentUserId);
		participant.setJoinedAt(time);
		
		conversationParticipantMapper.insertParticipant(participant);
		return conversationId;
	}
	
	public List<ConversationSummaryResponse> getConversationSummaries(UUID currentUserId) {
		return conversationMapper.findConversationSummaries(currentUserId);
	}
	
	public void validateParticipant(UUID conversationId, UUID userId) {
		boolean exists = conversationParticipantMapper.existsParticipant(conversationId, userId);
		if (!exists) {
			throw new IllegalArgumentException("User is not in conversation");
		}
	}
	private UUID normalizeUser1(UUID a, UUID b) {
		return a.compareTo(b) <= 0 ? a : b;
	}
	
	private UUID normalizeUser2(UUID a, UUID b) {
		return a.compareTo(b) <= 0 ? b : a;
	}
}
