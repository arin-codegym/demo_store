package com.quochuy.store.controller;

import com.quochuy.redis.dto.AuthState;
import com.quochuy.redis.service.AuthStateCache;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/debug/auth-state")
class DebugAuthStateController {
	private final AuthStateCache cache;
	
	DebugAuthStateController(AuthStateCache cache) {
		this.cache = cache;
	}
	
	@GetMapping("/{userId}")
	AuthState get(@PathVariable UUID userId) {
		return cache.get(userId);
	}
	
	@PostMapping("/{userId}")
	void set(@PathVariable UUID userId, @RequestBody AuthState state) {
		cache.set(userId, state);
	}
}
