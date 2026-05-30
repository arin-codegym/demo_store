package com.quochuy.chat.message.listener;

import com.quochuy.chat.conversation.mapper.ConversationParticipantMapper;
import com.quochuy.chat.message.dto.MessageResponse;
import com.quochuy.chat.message.event.ConversationSeenEvent;
import com.quochuy.chat.message.event.MessageCreatedEvent;
import com.quochuy.chat.message.model.Message;
import com.quochuy.chat.message.realtime.MessageCreatedRealtimeDispatcher;
import com.quochuy.chat.message.realtime.MessageCreatedRealtimePayload;
import com.quochuy.chat.message.service.MessageService;
import com.quochuy.chat.realtime.ChatRealtimePublisher;
import com.quochuy.websocket.dto.WsEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//@Component
//@RequiredArgsConstructor
//@Log4j2
//public class MessageCreatedRealtimeListener {
//	private final ChatRealtimePublisher chatRealtimePublisher;
//	private final MessageService messageService;
//	private final ConversationParticipantMapper conversationParticipantMapper;
//
//	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//	public void handleMessageCreated(MessageCreatedEvent event) {
//		log.debug(
//				"[event] handleMessageCreated fired, conversationId=" + event.getConversationId() + ", messageId=" + event.getMessageId());
//		Message message = messageService.findById(event.getMessageId());
//		if (message == null) {
//			log.error("[event] message is null");
//			return;
//		}
//		MessageResponse payload = MessageResponse.builder()
//												.messageId(message.getMessageId())
//												.conversationId(message.getConversationId())
//												.senderUserId(message.getSenderUserId())
//												.senderType(message.getSenderType())
//												.clientMessageId(message.getClientMessageId())
//												.content(message.getContent())
//												.status(message.getStatus().name())
//												.createdAt(message.getCreatedAt())
//												.build();
//		WsEnvelope envelope = WsEnvelope.builder()
//										.type("message.created")
//										.data(payload)
//										.build();
//		log.debug("[event] publishToConversation start");
//		chatRealtimePublisher.publishToConversation(event.getConversationId(), envelope);
//		log.debug("[event] publishToConversation done");
//		List<UUID> participantUserIds = conversationParticipantMapper.findUserIdsByConversationId(
//				event.getConversationId());
//		log.debug("[event] participants = " + participantUserIds);
//		for (UUID userId : participantUserIds) {
//			log.debug("[event] publishing sidebar to userId=" + userId);
//			chatRealtimePublisher.publishConversationSidebar(userId, WsEnvelope.builder()
//					.type("conversation.updated")
//					.data(Map.of("conversationId", event.getConversationId(), "lastMessageId",
//								 message.getMessageId(), "lastMessageContent", message.getContent(),
//								 "lastMessageAt", message.getCreatedAt())).build());
//			log.debug("[event] published sidebar to userId=" + userId);
//		}
//	}
//
//	@EventListener
//	public void handleConversationSeen(ConversationSeenEvent event) {
//		WsEnvelope envelope = WsEnvelope.builder().type("conversation.seen")
//				.data(Map.of("conversationId", event.getConversationId(), "userId",
//							 event.getUserId(), "lastReadMessageId", event.getLastReadMessageId(),
//							 "seenAt", Instant.now())).build();
//		chatRealtimePublisher.publishToConversation(event.getConversationId(), envelope);
//	}
//}

/**
 * Fallback realtime listener cho môi trường chưa bật RabbitMQ AMQP event queue.
 *
 * Khi app.chat.rabbit-events.enabled=false:
 * - MessageService publish Spring event trong transaction
 * - listener này chờ AFTER_COMMIT
 * - sau đó bắn WebSocket trực tiếp qua STOMP relay/simple broker
 *
 * Khi app.chat.rabbit-events.enabled=true:
 * - class này bị tắt
 * - MessageCreatedRabbitBridge sẽ đưa event vào RabbitMQ AMQP queue
 * - MessageCreatedRealtimeConsumer consume queue rồi gọi dispatcher
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
		prefix = "app.chat.rabbit-events",
		name = "enabled",
		havingValue = "false",
		matchIfMissing = true
)
public class MessageCreatedRealtimeListener {
	
	private final MessageCreatedRealtimeDispatcher dispatcher;
	
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleMessageCreated(MessageCreatedEvent event) {
		dispatcher.publish(MessageCreatedRealtimePayload.from(event));
	}
}