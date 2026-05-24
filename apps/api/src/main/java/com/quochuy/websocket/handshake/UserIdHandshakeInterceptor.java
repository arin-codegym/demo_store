package com.quochuy.websocket.handshake;

import java.util.Map;

import com.quochuy.security.CustomUserDetails;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

public class UserIdHandshakeInterceptor implements HandshakeInterceptor {
	
	@Override
	public boolean beforeHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Map<String, Object> attributes) {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		System.out.println("WS auth = " + auth);
		System.out.println("WS principal = " + (auth != null ? auth.getPrincipal() : null));
		
		if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
			System.out.println("WS userId = " + userDetails.getUserId());
			attributes.put("wsUserId", userDetails.getUserId().toString());
		}
		
		return true;
	}
	
	@Override
	public void afterHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Exception ex) {
	}
}
