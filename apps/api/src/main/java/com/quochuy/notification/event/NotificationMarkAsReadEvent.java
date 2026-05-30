package com.quochuy.notification.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationMarkAsReadEvent(UUID recipientUserId, UUID notificationId,
										  OffsetDateTime readAt) {
}
