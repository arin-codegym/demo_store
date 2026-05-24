package com.quochuy.notification.dto;

import lombok.Data;

import java.util.Map;
import java.util.UUID;
@Data
public class CreateNotificationCommand {
	private UUID recipientUserId;
	private String type;
	private String title;
	private String body;
	private UUID actorUserId;
	private String targetType;
	private UUID targetId;
	private String targetUrl;
	private String imageUrl;
	private Map<String, Object> metadata;
}
