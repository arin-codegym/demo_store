package com.quochuy.chat.conversation.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserChatItemResponse {
	private UUID userId;
	private String username;
	private String avatarUrl;
}
