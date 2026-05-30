package com.quochuy.websocket.publisher;

import com.quochuy.notification.event.NotificationCreatedEvent;
import com.quochuy.notification.event.NotificationMarkAllAsReadEvent;
import com.quochuy.notification.event.NotificationMarkAsReadEvent;
import com.quochuy.websocket.dto.WsEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class StompNotificationRealtimePublisher  {
	private final SimpMessagingTemplate messagingTemplate;
	
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publishCreated(NotificationCreatedEvent notificationCreatedEvent) {
		WsEnvelope envelope = new WsEnvelope(
				"notification.created",
				Map.of("notification", notificationCreatedEvent.notification())
		);
		
		messagingTemplate.convertAndSendToUser(
				notificationCreatedEvent.recipientUserId().toString(),
				"/queue/notifications",
				envelope
		);
	}
	
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publishRead(NotificationMarkAsReadEvent notificationMarkAsReadEvent) {
		WsEnvelope envelope = new WsEnvelope(
				"notification.read",
				Map.of(
						"notificationId", notificationMarkAsReadEvent.notificationId(),
						"userId", notificationMarkAsReadEvent.recipientUserId(),
						"readAt", notificationMarkAsReadEvent.readAt()
				)
		);
		
		messagingTemplate.convertAndSendToUser(
				notificationMarkAsReadEvent.recipientUserId().toString(),
				"/queue/notifications",
				envelope
		);
	}
	
	
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publishReadAll(NotificationMarkAllAsReadEvent notificationMarkAllAsReadEvent) {
		WsEnvelope envelope = new WsEnvelope(
				"notification.read-all",
				Map.of(
						"userId", notificationMarkAllAsReadEvent.recipientUserId(),
						"readAt", notificationMarkAllAsReadEvent.readAt()
				)
		);
		
		messagingTemplate.convertAndSendToUser(
				notificationMarkAllAsReadEvent.recipientUserId().toString(),
				"/queue/notifications",
				envelope
		);
	}
}
