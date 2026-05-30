package com.quochuy.notification.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationMarkAllAsReadEvent(UUID recipientUserId, OffsetDateTime readAt) {
}
