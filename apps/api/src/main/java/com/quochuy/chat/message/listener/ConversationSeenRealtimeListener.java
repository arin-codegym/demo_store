package com.quochuy.chat.message.listener;

import com.quochuy.chat.message.event.ConversationSeenEvent;
import com.quochuy.chat.realtime.ChatRealtimePublisher;
import com.quochuy.websocket.dto.WsEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.Map;

/**
 * Realtime event cho trạng thái seen.
 *
 * Dùng AFTER_COMMIT để tránh bắn socket trước khi update lastReadMessageId commit vào DB.
 */
@Component
@RequiredArgsConstructor
public class ConversationSeenRealtimeListener {

    private final ChatRealtimePublisher chatRealtimePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleConversationSeen(ConversationSeenEvent event) {
        WsEnvelope envelope = WsEnvelope.builder()
                .type("conversation.seen")
                .data(Map.of(
                        "conversationId", event.getConversationId(),
                        "userId", event.getUserId(),
                        "lastReadMessageId", event.getLastReadMessageId(),
                        "seenAt", Instant.now()
                ))
                .build();

        chatRealtimePublisher.publishToConversation(event.getConversationId(), envelope);
    }
}
