package com.quochuy.redis.dto;

import com.quochuy.redis.model.UserStatus;

public record AuthState(UserStatus status, int ver) {
}
