package com.quochuy.chat.message.rabbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quochuy.chat.message.realtime.MessageCreatedRealtimeDispatcher;
import com.quochuy.chat.message.realtime.MessageCreatedRealtimePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Consume message.created từ RabbitMQ AMQP rồi publish realtime ra WebSocket.
 *
 * Với 2 EC2 backend cùng listen queue này, RabbitMQ sẽ giao mỗi event cho một instance xử lý.
 * Instance nào xử lý cũng được vì bước cuối vẫn publish qua RabbitMQ STOMP relay trung tâm.
 */
@Component
@RequiredArgsConstructor
@Log4j2
@ConditionalOnProperty(prefix = "app.chat.rabbit-events", name = "enabled", havingValue = "true")
public class MessageCreatedRealtimeConsumer {

    private final ObjectMapper objectMapper;
    private final MessageCreatedRealtimeDispatcher dispatcher;

    @RabbitListener(queues = RabbitEventConfig.MESSAGE_CREATED_QUEUE)
    public void handleMessageCreated(String rawPayload) throws JsonProcessingException {
        MessageCreatedRealtimePayload payload = objectMapper.readValue(
                rawPayload,
                MessageCreatedRealtimePayload.class
        );

        log.debug("[rabbit] consume message.created, conversationId={}, messageId={}",
                payload.getConversationId(), payload.getMessageId());

        dispatcher.publish(payload);
    }
}
