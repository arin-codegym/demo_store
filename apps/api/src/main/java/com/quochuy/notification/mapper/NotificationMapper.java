package com.quochuy.notification.mapper;

import com.quochuy.notification.model.Notification;
import org.apache.ibatis.annotations.Mapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Mapper
public interface NotificationMapper {
	int insert(Notification notification);
	
	List<Notification> findNotifications(UUID recipientUserId, Boolean unreadOnly,
										 OffsetDateTime cursorCreatedAt, String cursorId,
										 int limit);
	
	long countUnread(UUID recipientUserId);
	
	Notification findByIdAndRecipientUserId(UUID notificationId, UUID recipientUserId);
	
	int markAsRead(UUID notificationId, UUID recipientUserId, OffsetDateTime readAt);
	
	int markAllAsRead(UUID recipientUserId, OffsetDateTime readAt);
	
	int markReadByConversation(UUID conversationId,UUID userId);
}
