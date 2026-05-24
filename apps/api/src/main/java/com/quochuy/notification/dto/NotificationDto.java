package com.quochuy.notification.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class NotificationDto {
	private UUID notificationId;
	private String type;
	private String title;
	private String body;
	private boolean read;
	private OffsetDateTime readAt;
	private OffsetDateTime createdAt;
	private ActorDto actor;
	private TargetDto target;
	private String imageUrl;
	private Map<String, Object> metadata;
	
	@Data
	public static class ActorDto {
		private UUID id;
		private String name;
		private String avatarUrl;
	}
	
	@Data
	public static class TargetDto {
		private String type;
		private UUID id;
		private String url;
	}
}
