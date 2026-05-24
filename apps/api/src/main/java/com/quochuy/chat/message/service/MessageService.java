package com.quochuy.chat.message.service;

import com.quochuy.chat.conversation.mapper.ConversationMapper;
import com.quochuy.chat.conversation.mapper.ConversationParticipantMapper;
import com.quochuy.chat.conversation.service.ConversationService;
import com.quochuy.chat.message.dto.MessageResponse;
import com.quochuy.chat.message.enums.MessageSenderType;
import com.quochuy.chat.message.enums.MessageStatus;
import com.quochuy.chat.message.event.MessageCreatedEvent;
import com.quochuy.chat.message.mapper.MessageMapper;
import com.quochuy.chat.message.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Xử lý nghiệp vụ gửi và đọc message.
 *
 * Class này chỉ lưu message, update last message, update read state
 * và phát domain event MessageCreatedEvent.
 *
 * Realtime, notification, AI reply không xử lý trực tiếp ở đây.
 * Các phần đó được xử lý bởi listener riêng.
 */
@Service
@RequiredArgsConstructor
public class MessageService {
	private final MessageMapper messageMapper;
	private final ConversationMapper conversationMapper;
	private final ConversationParticipantMapper conversationParticipantMapper;
	private final ConversationService conversationService;
	private final ApplicationEventPublisher eventPublisher;
	
	@Transactional
	public MessageResponse sendMessage(UUID currentUserId, UUID conversationId,
									   String clientMessageId, String content) {
		conversationService.validateParticipant(conversationId, currentUserId);
		Message existed = messageMapper.findBySenderAndClientMessageId(currentUserId,
																	   clientMessageId);
		if (existed != null) {
			return toResponse(existed);
		}
		Message message = new Message();
		message.setMessageId(UUID.randomUUID());
		message.setConversationId(conversationId);
		message.setSenderUserId(currentUserId);
		message.setSenderType(MessageSenderType.USER);
		message.setClientMessageId(clientMessageId);
		message.setContent(content);
		message.setStatus(MessageStatus.SENT);
		message.setCreatedAt(OffsetDateTime.now());
		messageMapper.insertMessage(message);
		conversationMapper.updateLastMessage(conversationId, message.getMessageId());
		conversationParticipantMapper.updateLastReadMessageId(conversationId,currentUserId,
															  message.getMessageId());
		eventPublisher.publishEvent(
				new MessageCreatedEvent(message.getMessageId(), conversationId, currentUserId, false));
		return toResponse(message);
	}
	
	public List<MessageResponse> getMessages(UUID currentUserId, UUID conversationId, UUID beforeMessageId,
									 Integer limit) {
		conversationService.validateParticipant(conversationId, currentUserId);
		return messageMapper.findMessagesByConversationId(conversationId, beforeMessageId, limit).stream()
				.map(this::toResponse).toList();
	}
	
	public Message findById(UUID id) {
		return messageMapper.findById(id);
	}
	
	private MessageResponse toResponse(Message message) {
		return MessageResponse.builder()
				.messageId(message.getMessageId())
				.conversationId(message.getConversationId())
				.senderUserId(message.getSenderUserId())
				.senderType(message.getSenderType())
				.clientMessageId(message.getClientMessageId())
				.content(message.getContent())
				.status(message.getStatus().name())
				.createdAt(message.getCreatedAt())
				.build();
	}
	
}
