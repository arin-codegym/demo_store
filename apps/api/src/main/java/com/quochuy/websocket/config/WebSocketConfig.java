package com.quochuy.websocket.config;

import com.quochuy.websocket.handshake.UserIdHandshakeHandler;
import com.quochuy.websocket.handshake.UserIdHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	@Value("${app.websocket.relay.enabled:false}")
	private boolean relayEnabled;
	
	@Value("${app.websocket.relay.host:localhost}")
	private String relayHost;
	
	@Value("${app.websocket.relay.port:61613}")
	private int relayPort;
	
	@Value("${app.websocket.relay.login:guest}")
	private String relayLogin;
	
	@Value("${app.websocket.relay.passcode:guest}")
	private String relayPasscode;
	
	@Value("${app.websocket.relay.virtual-host:/}")
	private String relayVirtualHost;
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		if (relayEnabled) {
			registry.enableStompBrokerRelay("/topic", "/queue")
					.setRelayHost(relayHost)
					.setRelayPort(relayPort)
					.setClientLogin(relayLogin)
					.setClientPasscode(relayPasscode)
					.setSystemLogin(relayLogin)
					.setSystemPasscode(relayPasscode)
					.setVirtualHost(relayVirtualHost)
					.setUserDestinationBroadcast("/topic/unresolved-user-destination") //The backend servers share a list of websocket users currently online.
					.setUserRegistryBroadcast("/topic/simp-user-registry");;//If a node cannot find the user, it broadcasts the message so another node can send it.;
		} else {
			registry.enableSimpleBroker("/topic", "/queue");
		}
		
		registry.setApplicationDestinationPrefixes("/app");
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
