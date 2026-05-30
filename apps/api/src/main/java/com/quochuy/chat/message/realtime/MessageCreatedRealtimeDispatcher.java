package com.quochuy.chat.message.realtime;

import com.quochuy.chat.conversation.mapper.ConversationParticipantMapper;
import com.quochuy.chat.message.dto.MessageResponse;
import com.quochuy.chat.message.model.Message;
import com.quochuy.chat.message.service.MessageService;
import com.quochuy.chat.realtime.ChatRealtimePublisher;
import com.quochuy.websocket.dto.WsEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Build payload realtime từ dữ liệu đã commit trong DB rồi publish ra WebSocket.
 *
 * Class này cố tình không phụ thuộc vào cách event được kích hoạt.
 * Event có thể đến từ Spring AFTER_COMMIT trực tiếp hoặc từ RabbitMQ AMQP consumer.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class MessageCreatedRealtimeDispatcher {

    private final ChatRealtimePublisher chatRealtimePublisher;
    private final MessageService messageService;
    private final ConversationParticipantMapper conversationParticipantMapper;

    public void publish(MessageCreatedRealtimePayload event) {
        log.debug("[realtime] message.created start, conversationId={}, messageId={}",
                event.getConversationId(), event.getMessageId());

        Message message = messageService.findById(event.getMessageId());
        if (message == null) {
            log.error("[realtime] message.created skipped because message not found, messageId={}",
                    event.getMessageId());
            return;
        }

        MessageResponse payload = MessageResponse.builder()
                .messageId(message.getMessageId())
                .conversationId(message.getConversationId())
                .senderUserId(message.getSenderUserId())
                .senderType(message.getSenderType())
                .clientMessageId(message.getClientMessageId())
                .content(message.getContent())
                .status(message.getStatus().name())
                .createdAt(message.getCreatedAt())
                .build();

        WsEnvelope messageEnvelope = WsEnvelope.builder()
                .type("message.created")
                .data(payload)
                .build();

        chatRealtimePublisher.publishToConversation(event.getConversationId(), messageEnvelope);

        List<UUID> participantUserIds = conversationParticipantMapper.findUserIdsByConversationId(
                event.getConversationId());

        WsEnvelope sidebarEnvelope = WsEnvelope.builder()
                .type("conversation.updated")
                .data(Map.of(
                        "conversationId", event.getConversationId(),
                        "lastMessageId", message.getMessageId(),
                        "lastMessageContent", message.getContent(),
                        "lastMessageAt", message.getCreatedAt()
                ))
                .build();

        for (UUID userId : participantUserIds) {
            chatRealtimePublisher.publishConversationSidebar(userId, sidebarEnvelope);
        }

        log.debug("[realtime] message.created done, conversationId={}, messageId={}",
                event.getConversationId(), event.getMessageId());
    }
}
