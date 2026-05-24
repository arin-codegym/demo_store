package com.quochuy.websocket.publisher;

import com.quochuy.notification.realtime.NotificationRealtimePublisher;
import com.quochuy.websocket.dto.WsEnvelope;
import com.quochuy.notification.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StompNotificationRealtimePublisher implements NotificationRealtimePublisher {
	private final SimpMessagingTemplate messagingTemplate;
	@Override
	public void publishCreated(UUID recipientUserId, NotificationDto notification) {
		WsEnvelope envelope = new WsEnvelope(
				"notification.created",
				Map.of("notification", notification)
		);
		
		messagingTemplate.convertAndSendToUser(
				recipientUserId.toString(),
				"/queue/notifications",
				envelope
		);
	}
	
	@Override
	public void publishRead(UUID recipientUserId, UUID notificationId, OffsetDateTime readAt) {
		WsEnvelope envelope = new WsEnvelope(
				"notification.read",
				Map.of(
						"notificationId", notificationId,
						"userId", recipientUserId,
						"readAt", readAt
				)
		);
		
		messagingTemplate.convertAndSendToUser(
				recipientUserId.toString(),
				"/queue/notifications",
				envelope
		);
	}
	
	@Override
	public void publishReadAll(UUID recipientUserId, OffsetDateTime readAt) {
		WsEnvelope envelope = new WsEnvelope(
				"notification.read-all",
				Map.of(
						"userId", recipientUserId,
						"readAt", readAt
				)
		);
		
		messagingTemplate.convertAndSendToUser(
				recipientUserId.toString(),
				"/queue/notifications",
				envelope
		);
	}
}
