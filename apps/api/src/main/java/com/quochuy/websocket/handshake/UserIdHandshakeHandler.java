package com.quochuy.websocket.handshake;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

public class UserIdHandshakeHandler extends DefaultHandshakeHandler {
	
	@Override
	protected Principal determineUser(
			ServerHttpRequest request,
			WebSocketHandler wsHandler,
			Map<String, Object> attributes) {
		
		Object userId = attributes.get("wsUserId");
		System.out.println("determineUser wsUserId = " + userId);
		
		if (userId == null) {
			return null;
		}
		
		String principalName = userId.toString();
		return () -> principalName;
	}
}
