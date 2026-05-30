package com.quochuy.chat.message.realtime;

import com.quochuy.chat.message.event.MessageCreatedEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Payload nội bộ dùng cho luồng realtime message.created.
 *
 * Class này được dùng ở cả 2 mode:
 * - direct mode: Spring event AFTER_COMMIT gọi thẳng dispatcher
 * - rabbit mode: AFTER_COMMIT đưa payload vào RabbitMQ AMQP, consumer đọc lại rồi gọi dispatcher
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreatedRealtimePayload {
    private UUID messageId;
    private UUID conversationId;
    private UUID senderUserId;
    private boolean aiMessage;

    public static MessageCreatedRealtimePayload from(MessageCreatedEvent event) {
        return MessageCreatedRealtimePayload.builder()
                .messageId(event.getMessageId())
                .conversationId(event.getConversationId())
                .senderUserId(event.getSenderUserId())
                .aiMessage(event.isAiMessage())
                .build();
    }
}
