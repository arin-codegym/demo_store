package com.quochuy.notification.controller;

import com.quochuy.security.CustomUserDetails;
import com.quochuy.chat.message.dto.MarkReadByConversationRequest;
import com.quochuy.notification.dto.NotificationDto;
import com.quochuy.notification.dto.NotificationListResponse;
import com.quochuy.notification.dto.UnreadNotificationCountResponse;
import com.quochuy.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/backend/notifications")

public class NotificationController {
	
	private final NotificationService notificationService;
	
	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}
	
	@GetMapping
	public NotificationListResponse getNotifications(
			@AuthenticationPrincipal CustomUserDetails principal,
			@RequestParam(required = false) String cursor,
			@RequestParam(defaultValue = "20") int limit,
			@RequestParam(defaultValue = "false") boolean unreadOnly
	) {
		return notificationService.getNotifications(
				principal.getUserId(),
				cursor,
				limit,
				unreadOnly
		);
	}
	
	@GetMapping("/unread-count")
	public UnreadNotificationCountResponse getUnreadCount(
			@AuthenticationPrincipal CustomUserDetails principal
	) {
		long unreadCount = notificationService.getUnreadCount(principal.getUserId());
		return new UnreadNotificationCountResponse(unreadCount);
	}
	
	@PostMapping("/{notificationId}/read")
	public NotificationDto markAsRead(
			@AuthenticationPrincipal CustomUserDetails principal,
			@PathVariable UUID notificationId
	) {
		return notificationService.markAsRead(principal.getUserId(), notificationId);
	}
	
	@PostMapping("/read-all")
	public ResponseEntity<Void> markAllAsRead(
			@AuthenticationPrincipal CustomUserDetails principal
	) {
		notificationService.markAllAsRead(principal.getUserId());
		return ResponseEntity.ok().build();
	}
	@PostMapping("/mark-read-by-conversation")
	public ResponseEntity<Void> markReadByConversation(
			@AuthenticationPrincipal CustomUserDetails principal,
			@RequestBody MarkReadByConversationRequest reqBody
	) {
		System.out.println("Marking notifications as read for conversationId=" + reqBody.getConversationId() + ", userId=" + principal.getUserId());
		notificationService.markReadByConversation(reqBody.getConversationId(),principal.getUserId());
		return ResponseEntity.ok().build();
	}
}
