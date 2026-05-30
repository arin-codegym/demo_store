package com.quochuy.notification.realtime;

import com.quochuy.notification.dto.NotificationDto;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface NotificationRealtimePublisher {
	
	
	void publishCreated(UUID recipientUserId, NotificationDto notification);
	void publishRead(UUID recipientUserId, UUID notificationId, OffsetDateTime readAt);
	void publishReadAll(UUID recipientUserId, OffsetDateTime readAt);
}
