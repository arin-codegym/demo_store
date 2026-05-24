package com.quochuy.chat.realtime;

import java.util.UUID;

public interface ChatRealtimePublisher {
	void publishToConversation(UUID conversationId, Object payload);
	
	void publishToUser(Long userId, Object payload);
	
	void publishConversationSidebar(UUID userId, Object payload);
}
