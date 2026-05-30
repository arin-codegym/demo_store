package com.quochuy.notification.event;

import com.quochuy.notification.dto.NotificationDto;

import java.util.UUID;

public record NotificationCreatedEvent(UUID recipientUserId,
									   NotificationDto notification) {
}
