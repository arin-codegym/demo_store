package com.quochuy.notification.listener;

import com.quochuy.chat.conversation.enums.ConversationType;
import com.quochuy.chat.conversation.mapper.ConversationMapper;
import com.quochuy.chat.conversation.mapper.ConversationParticipantMapper;
import com.quochuy.chat.message.mapper.MessageMapper;
import com.quochuy.store.service.impl.UserServiceImpl;
import com.quochuy.notification.dto.CreateNotificationCommand;
import com.quochuy.notification.enums.NotificationTypes;
import com.quochuy.chat.message.event.MessageCreatedEvent;
import com.quochuy.chat.conversation.model.Conversation;
import com.quochuy.chat.message.model.Message;
import com.quochuy.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageCreatedNotificationListener {
	private final ConversationParticipantMapper conversationParticipantMapper;
	private final NotificationService notificationService;
	private final MessageMapper messageMapper;
	private final ConversationMapper conversationMapper;
	
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(MessageCreatedEvent event) {
		List<UUID> recipientUserIds = resolveRecipientUserId(event.getConversationId(),
															 event.getSenderUserId());
		if (recipientUserIds == null || recipientUserIds.isEmpty()) {
			return;
		}
		Message message = messageMapper.findById(event.getMessageId());
		if (message == null) {
			return;
		}
		Conversation conversation = conversationMapper.findById(event.getConversationId());
		if (conversation == null) {
			return;
		}
		
		final ConversationType conversationType = conversation.getType();
		for (UUID recipientUserId : recipientUserIds) {
			CreateNotificationCommand command = new CreateNotificationCommand();
			command.setRecipientUserId(recipientUserId);
			command.setType(NotificationTypes.CHAT_MESSAGE_CREATED);
			command.setTitle("Tin nhắn mới");
			command.setBody(message.getContent());
			command.setActorUserId(event.getSenderUserId());
			
			command.setTargetType(conversationType.name());
			command.setTargetId(event.getConversationId());
			
			// Chỉ gán URL cho loại chat đi bằng page /chat
			if (ConversationType.USER_DIRECT == conversationType) {
				command.setTargetUrl("/chat?conversationId=" + event.getConversationId());
			} else {
				command.setTargetUrl(null);
			}
			
			command.setMetadata(Map.of(
					"conversationId",
					event.getConversationId(),
					"messageId",
					event.getMessageId()
			));
			
			notificationService.createNotification(command);
		}
	}
	
	private List<UUID> resolveRecipientUserId(UUID conversationId, UUID senderUserId) {
		List<UUID> recipientUserIds = conversationParticipantMapper.findUserIdsByConversationId(
				conversationId).stream().filter(uuid -> !uuid.equals(senderUserId)).toList();
		return recipientUserIds;
	}
}
