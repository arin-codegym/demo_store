package com.quochuy.redis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quochuy.redis.dto.AuthState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
public class AuthStateCache {
	private static final Duration TTL = Duration.ofMinutes(5);
	private final StringRedisTemplate redis;
	private final ObjectMapper om ; // dùng bean của spring xin có sẳn không tạo bean mới
	
	public AuthStateCache(StringRedisTemplate redis, ObjectMapper om) {
		this.redis = redis;
		this.om = om;
	}
	
	private String key(UUID userId) {
		return "auth:state:" + userId;
	}
	
	public AuthState get(UUID userId) {
		try {
			String json = redis.opsForValue().get(key(userId));
			if (json == null) return null;
			return om.readValue(json, AuthState.class);
		} catch (Exception e) {
			log.warn("Failed to read auth state cache for user {}: {}", userId, e.getMessage());
			return null; // fail-open cache layer
		}
	}
	
	public void set(UUID userId, AuthState state) {
		try {
			String json = om.writeValueAsString(state);
			redis.opsForValue().set(key(userId), json, TTL);
		} catch (Exception e) {
			log.warn("Failed to write auth state cache for user {}: {}", userId, e.getMessage());
		}
	}
	// (optional) khi muốn “tức thì”: ban/unban/update ver rồi set lại Redis
	public void setWithTtl(UUID userId, AuthState state, Duration ttl) {
		try {
			redis.opsForValue().set(key(userId), om.writeValueAsString(state), ttl);
		} catch (Exception e) {
			log.warn("Failed to write auth state cache for user {}: {}", userId, e.getMessage());
		}
	}
	public void delete(UUID userId) {
		redis.delete(key(userId));
	}
}
