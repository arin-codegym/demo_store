package com.quochuy.notification.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quochuy.notification.dto.CreateNotificationCommand;
import com.quochuy.notification.dto.NotificationDto;
import com.quochuy.notification.dto.NotificationListResponse;
import com.quochuy.notification.event.NotificationCreatedEvent;
import com.quochuy.notification.event.NotificationMarkAllAsReadEvent;
import com.quochuy.notification.event.NotificationMarkAsReadEvent;
import com.quochuy.notification.mapper.NotificationMapper;
import com.quochuy.notification.model.Notification;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private final NotificationMapper notificationMapper;
	private final ObjectMapper objectMapper;
	private final ModelMapper modelMapper;
	private final ApplicationEventPublisher eventPublisher;
	
	@Transactional
	public void createNotification(CreateNotificationCommand command) {
		Notification notification = new Notification();
		notification.setNotificationId(UUID.randomUUID());
		notification.setRecipientUserId(command.getRecipientUserId());
		notification.setType(command.getType());
		notification.setTitle(command.getTitle());
		notification.setBody(command.getBody());
		notification.setRead(false);
		notification.setReadAt(null);
		notification.setActorUserId(command.getActorUserId());
		notification.setTargetType(command.getTargetType());
		notification.setTargetId(command.getTargetId());
		notification.setTargetUrl(command.getTargetUrl());
		notification.setImageUrl(command.getImageUrl());
		notification.setMetadataJson(writeJson(command.getMetadata()));
		notification.setCreatedAt(OffsetDateTime.now());
		notificationMapper.insert(notification);
		NotificationDto dto = toDto(notification);
		eventPublisher.publishEvent(new NotificationCreatedEvent(command.getRecipientUserId(), dto));
	}
	
	
	@Transactional(readOnly = true)
	public NotificationListResponse getNotifications(UUID recipientUserId, String cursor,
													 int limit, boolean unreadOnly) {
		CursorParts cursorParts = parseCursor(cursor);
		List<Notification> rows = notificationMapper.findNotifications(recipientUserId, unreadOnly,
																	   cursorParts.createdAt(),
																	   cursorParts.id(), limit);
		List<NotificationDto> items = rows.stream().map(this::toDto).toList();
		String nextCursor = rows.size() < limit ? null : buildCursor(rows.get(rows.size() - 1));
		NotificationListResponse response = new NotificationListResponse();
		response.setItems(items);
		response.setNextCursor(nextCursor);
		return response;
	}
	
	@Transactional(readOnly = true)
	public long getUnreadCount(UUID recipientUserId) {
		return notificationMapper.countUnread(recipientUserId);
	}
	
	@Transactional
	public NotificationDto markAsRead(UUID recipientUserId, UUID notificationId) {
		OffsetDateTime readAt = OffsetDateTime.now();
		notificationMapper.markAsRead(notificationId, recipientUserId, readAt);
		Notification row = notificationMapper.findByIdAndRecipientUserId(notificationId,
																		 recipientUserId);
		if (row == null) {
			throw new RuntimeException("Notification not found");
		}
		NotificationDto dto = toDto(row);
		eventPublisher.publishEvent(new NotificationMarkAsReadEvent(recipientUserId, notificationId, row.getReadAt()));
		return dto;
	}
	
	@Transactional
	public void markAllAsRead(UUID recipientUserId) {
		OffsetDateTime readAt = OffsetDateTime.now();
		notificationMapper.markAllAsRead(recipientUserId, readAt);
		eventPublisher.publishEvent(new NotificationMarkAllAsReadEvent(recipientUserId, readAt));
	}
	
	private NotificationDto toDto(Notification row) {
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
//		NotificationDto dto = new NotificationDto();
//		dto.setId(row.getNotificationId());
//		dto.setType(row.getType());
//		dto.setTitle(row.getTitle());
//		dto.setBody(row.getBody());
//		dto.setRead(row.isRead());
//		dto.setReadAt(row.getReadAt());
//		dto.setCreatedAt(row.getCreatedAt());
//		dto.setImageUrl(row.getImageUrl());
//
//		if (row.getTargetType() != null || row.getTargetId() != null || row.getTargetUrl() != null) {
//			NotificationDto.TargetDto target = new NotificationDto.TargetDto();
//			target.setType(row.getTargetType());
//			target.setId(row.getTargetId());
//			target.setUrl(row.getTargetUrl());
//			dto.setTarget(target);
//		}
//
//		dto.setMetadata(readJson(row.getMetadataJson()));
		NotificationDto dto = modelMapper.map(row, NotificationDto.class);
		if (row.getTargetType() != null || row.getTargetId() != null || row.getTargetUrl() != null) {
			NotificationDto.TargetDto target = new NotificationDto.TargetDto();
			target.setType(row.getTargetType());
			target.setId(row.getTargetId());
			target.setUrl(row.getTargetUrl());
			dto.setTarget(target);
		}
		dto.setMetadata(readJson(row.getMetadataJson()));
		return dto;
	}
	
	private String writeJson(Map<String, Object> metadata) {
		try {
			return metadata == null ? null : objectMapper.writeValueAsString(metadata);
		} catch (Exception e) {
			throw new RuntimeException("Cannot serialize notification metadata", e);
		}
	}
	
	private Map<String, Object> readJson(String metadataJson) {
		try {
			if (metadataJson == null || metadataJson.isBlank()) {
				return null;
			}
			return objectMapper.readValue(metadataJson, new TypeReference<>() {
			});
		} catch (Exception e) {
			throw new RuntimeException("Cannot deserialize notification metadata", e);
		}
	}
	
	private String buildCursor(Notification row) {
		return row.getCreatedAt() + "|" + row.getNotificationId();
	}
	
	private CursorParts parseCursor(String cursor) {
		if (cursor == null || cursor.isBlank()) {
			return new CursorParts(null, null);
		}
		String[] parts = cursor.split("\\|", 2);
		if (parts.length != 2) {
			throw new RuntimeException("Invalid cursor");
		}
		return new CursorParts(OffsetDateTime.parse(parts[0]), parts[1]);
	}
	
	public void markReadByConversation(UUID conversationId,UUID userId) {
		notificationMapper.markReadByConversation(conversationId, userId);
//		int updatedRows = notificationMapper.markReadByConversation(conversationId, userId);
//
//		System.out.println(
//				"markReadByConversation updatedRows=" + updatedRows
//						+ ", conversationId=" + conversationId
//						+ ", userId=" + userId
//		);
	}
	
	private record CursorParts(OffsetDateTime createdAt, String id) {
	}
}
