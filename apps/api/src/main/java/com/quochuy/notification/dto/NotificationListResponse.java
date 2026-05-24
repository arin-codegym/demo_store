package com.quochuy.notification.dto;

import lombok.Data;

import java.util.List;

@Data
public class NotificationListResponse {
	private List<NotificationDto> items;
	private String nextCursor;
}
