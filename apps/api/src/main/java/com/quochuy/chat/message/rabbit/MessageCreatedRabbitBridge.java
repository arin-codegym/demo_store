package com.quochuy.chat.message.rabbit;

import com.quochuy.chat.message.event.MessageCreatedEvent;
import com.quochuy.chat.message.realtime.MessageCreatedRealtimePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Cầu nối từ Spring domain event sang RabbitMQ AMQP.
 *
 * Chỉ chạy sau khi transaction insert message đã commit thành công.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.chat.rabbit-events",
        name = "enabled",
        havingValue = "true")
public class MessageCreatedRabbitBridge {

    private final MessageCreatedRabbitEventPublisher publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommit(MessageCreatedEvent event) {
        publisher.publish(MessageCreatedRealtimePayload.from(event));
    }
}
