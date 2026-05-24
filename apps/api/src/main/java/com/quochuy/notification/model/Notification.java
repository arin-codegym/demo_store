package com.quochuy.notification.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class Notification {
	private UUID notificationId;
	private UUID recipientUserId;
	private String type;
	private String title;
	private String body;
	private boolean read;
	private OffsetDateTime readAt;
	private UUID actorUserId;
	private String targetType;
	private UUID targetId;
	private String targetUrl;
	private String imageUrl;
	private String metadataJson;
	private OffsetDateTime createdAt;
}
