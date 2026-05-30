package com.quochuy.chat.message.rabbit;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ AMQP config cho event nội bộ của chat.
 *
 * Lưu ý: config này khác với RabbitMQ STOMP relay trong WebSocketConfig.
 * - AMQP port 5672: dùng cho RabbitTemplate / @RabbitListener
 * - STOMP port 61613: dùng cho WebSocket broker relay
 */
@Configuration
@EnableRabbit
@ConditionalOnProperty(prefix = "app.chat.rabbit-events", name = "enabled", havingValue = "true")
public class RabbitEventConfig {

    public static final String CHAT_EVENTS_EXCHANGE = "chat.events";
    public static final String MESSAGE_CREATED_QUEUE = "chat.message.created.realtime";
    public static final String MESSAGE_CREATED_ROUTING_KEY = "message.created";

    @Bean
    public TopicExchange chatEventsExchange() {
        return new TopicExchange(CHAT_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue messageCreatedQueue() {
        return QueueBuilder.durable(MESSAGE_CREATED_QUEUE).build();
    }

    @Bean
    public Binding messageCreatedBinding(Queue messageCreatedQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder
                .bind(messageCreatedQueue)
                .to(chatEventsExchange)
                .with(MESSAGE_CREATED_ROUTING_KEY);
    }
}
