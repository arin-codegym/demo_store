package com.quochuy.websocket.config;

import com.quochuy.websocket.handshake.UserIdHandshakeInterceptor;
import com.quochuy.websocket.handshake.UserIdHandshakeHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// Client subscribe các kênh broker này
		/**Cho phép client subscribe kiểu:
			 /topic/conversations/123
			 /user/queue/messages
		 * */
		registry.enableSimpleBroker("/topic", "/queue");
		
		// Nếu client gửi message vào app (nếu sau này cần)
		/** Nếu sau này client muốn gửi message qua STOMP tới backend thì dùng prefix /app.
		 Nhưng phase 1 của bạn chưa cần lấy WebSocket làm command path.
		 */
		registry.setApplicationDestinationPrefixes("/app");
		
		// Prefix cho user-specific queue
		/**Để dùng convertAndSendToUser(...).*/
		registry.setUserDestinationPrefix("/user");
	}
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws")
				.addInterceptors(new UserIdHandshakeInterceptor())
				.setHandshakeHandler(new UserIdHandshakeHandler())
				.setAllowedOriginPatterns("*")
		 .withSockJS();
	}
}
