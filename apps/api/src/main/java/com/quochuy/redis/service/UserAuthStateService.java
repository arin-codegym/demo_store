package com.quochuy.redis.service;

import com.quochuy.store.mapper.UserAuthStateMapper;
import com.quochuy.redis.dto.AuthState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAuthStateService {
	private final UserAuthStateMapper userAuthStateMapper;
	public AuthState loadAuthState(UUID userId) {
		return userAuthStateMapper.loadAuthState(userId);
	}
}
