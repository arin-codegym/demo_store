package com.quochuy.store.model;

import com.quochuy.redis.model.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class User {
	private UUID userId;
	private String userName;
	private String password;
	private String email;
	private String fullName;
	private String avatarUrl;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<String> roles;
	
	private String authProvider;  // LOCAL / GOOGLE
	private String providerUserId; // Google "sub"
	private UserStatus status;
	private int tokenVersion;
	private boolean emailVerified;
	
	public UUID getUserIdAsUuid() {
		return this.userId;
	}
}
