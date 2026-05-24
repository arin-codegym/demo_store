package com.quochuy.chat.conversation.service;

import com.quochuy.chat.conversation.enums.ConversationType;
import com.quochuy.chat.conversation.mapper.ConversationMapper;
import com.quochuy.chat.conversation.mapper.ConversationParticipantMapper;
import com.quochuy.chat.conversation.mapper.DirectConversationPairMapper;
import com.quochuy.store.mapper.UserMapper;
import com.quochuy.chat.conversation.dto.ConversationSummaryResponse;
import com.quochuy.chat.conversation.model.Conversation;
import com.quochuy.chat.conversation.model.ConversationParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationService {
	private final ConversationMapper conversationMapper;
	private final ConversationParticipantMapper conversationParticipantMapper;
	private final DirectConversationPairMapper directConversationPairMapper;
	private final UserMapper userMapper;
	
	@Transactional
	public UUID createOrGetDirectConversation(UUID currentUserId, UUID targetUserId) {
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
		UUID existedConversationId =
				directConversationPairMapper.findConversationIdByPair(user1, user2);
		
		if (existedConversationId != null) {
			return existedConversationId;
		}
		OffsetDateTime time = OffsetDateTime.now();
		Conversation conversation = new Conversation();
		conversation.setType(ConversationType.USER_DIRECT);
		conversation.setAssistantCode(null);
		conversation.setCreatedAt(time);
		conversation.setUpdatedAt(time);
		conversationMapper.insertConversation(conversation);
		
		try {
			directConversationPairMapper.insertPair(conversation.getConversationId(), user1, user2);
			ConversationParticipant p1 = new ConversationParticipant();
			p1.setConversationId(conversation.getConversationId());
			p1.setUserId(currentUserId);
			p1.setJoinedAt(time);
			ConversationParticipant p2 = new ConversationParticipant();
			p2.setConversationId(conversation.getConversationId());
			p2.setUserId(targetUserId);
			p2.setJoinedAt(time);
			conversationParticipantMapper.insertParticipant(p1);
			conversationParticipantMapper.insertParticipant(p2);
			return conversation.getConversationId();
		} catch (DuplicateKeyException e) {
			UUID existingId =
					directConversationPairMapper.findConversationIdByPair(user1, user2);
			
			if (existingId != null) {
				return existingId;
			}
			throw e;
		}
	}
	
	@Transactional
	public UUID createOrGetAiConversation(UUID currentUserId, String assistantCode) {
		if (currentUserId == null) {
			throw new IllegalArgumentException("User id must not be null");
		}
		
		if (assistantCode == null || assistantCode.isBlank()) {
			throw new IllegalArgumentException("Assistant code must not be blank");
		}
		
		if (userMapper.findById(currentUserId) == null) {
			throw new IllegalArgumentException("User does not exist");
		}
		
		UUID existedConversationId =
				conversationMapper.findAiConversationIdByUserIdAndAssistantCode(
						currentUserId,
						assistantCode
				);
		
		if (existedConversationId != null) {
			return existedConversationId;
		}
		OffsetDateTime time = OffsetDateTime.now();
		Conversation conversation = new Conversation();
		conversation.setType(ConversationType.USER_AI);
		conversation.setAssistantCode(assistantCode);
		conversation.setCreatedAt(time);
		conversation.setUpdatedAt(time);
		conversationMapper.insertConversation(conversation);
		
		ConversationParticipant participant = new ConversationParticipant();
		participant.setConversationId(conversation.getConversationId());
		participant.setUserId(currentUserId);
		participant.setJoinedAt(time);
		
		try {
			conversationParticipantMapper.insertParticipant(participant);
			return conversation.getConversationId();
		} catch (DuplicateKeyException e) {
			UUID existingId =
					conversationMapper.findAiConversationIdByUserIdAndAssistantCode(
							currentUserId,
							assistantCode
					);
			if (existingId != null) {
				return existingId;
			}
			throw e;
		}
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
	public ResponseEntity<?> getAdminConversation(UUID userId) {
		UUID adminId = userMapper.getAdminId();
		UUID conversationId = createOrGetDirectConversation(userId, adminId);
		return ResponseEntity.ok(Map.of("conversationId", conversationId));
	}
	
	public ResponseEntity<?> getAiConversation(UUID userId, String assistantCode) {
		UUID conversationId = createOrGetAiConversation(userId,assistantCode);
		return ResponseEntity.ok(Map.of("conversationId", conversationId));
	}
}
