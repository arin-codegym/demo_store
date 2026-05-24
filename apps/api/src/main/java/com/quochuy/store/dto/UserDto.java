package com.quochuy.store.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDto {
	private String userId;
	private String userName;
	private String email;
	private String fullName;
	private String avatarUrl;
	private List<String> roles;
}
