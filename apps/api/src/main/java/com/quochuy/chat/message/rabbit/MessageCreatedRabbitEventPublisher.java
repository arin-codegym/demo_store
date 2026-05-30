package com.quochuy.chat.message.rabbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quochuy.chat.message.realtime.MessageCreatedRealtimePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Publish message.created vào RabbitMQ AMQP queue sau khi DB commit.
 *
 * RabbitMQ ở đây là queue nội bộ backend, không phải bước gửi trực tiếp tới frontend.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.chat.rabbit-events",
        name = "enabled",
        havingValue = "true")
public class MessageCreatedRabbitEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void publish(MessageCreatedRealtimePayload payload) {
        try {
            String rawPayload = objectMapper.writeValueAsString(payload);
            rabbitTemplate.convertAndSend(
                    RabbitEventConfig.CHAT_EVENTS_EXCHANGE,
                    RabbitEventConfig.MESSAGE_CREATED_ROUTING_KEY,
                    rawPayload
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize message.created realtime payload", e);
        }
    }
}
