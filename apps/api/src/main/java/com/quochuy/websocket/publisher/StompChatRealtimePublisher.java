package com.quochuy.websocket.publisher;
import com.quochuy.chat.realtime.ChatRealtimePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementation của ChatRealtimePublisher bằng STOMP/WebSocket.
 *
 * Đây là "đầu ra realtime" của module chat:
 * - gửi message vào topic conversation
 * - gửi cập nhật sidebar conversation cho từng user
 *
 * Không chứa business logic chat.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class StompChatRealtimePublisher implements ChatRealtimePublisher {
	
	private final SimpMessagingTemplate messagingTemplate;
	
	@Override
	public void publishToConversation(UUID conversationId, Object payload) {
		log.debug("[ws] send topic /topic/conversations/" + conversationId + " payload=" + payload);
		messagingTemplate.convertAndSend("/topic/conversations/" + conversationId, payload);
	}
	
	@Override
	public void publishToUser(Long userId, Object payload) {
		messagingTemplate.convertAndSendToUser(
				String.valueOf(userId),
				"/queue/messages",
				payload
		);
	}
	
	@Override
	public void publishConversationSidebar(UUID userId, Object payload) {
		messagingTemplate.convertAndSendToUser(
				String.valueOf(userId),
				"/queue/conversations",
				payload
		);
	}
}
