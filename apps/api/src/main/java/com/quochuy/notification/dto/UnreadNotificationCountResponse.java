package com.quochuy.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UnreadNotificationCountResponse {
	private long unreadCount;
}
