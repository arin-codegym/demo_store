package com.quochuy.chat.conversation.mapper;

import com.quochuy.chat.conversation.model.Conversation;
import com.quochuy.chat.conversation.dto.ConversationSummaryResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ConversationMapper {
	int insertConversation(Conversation conversation);
	
	Conversation findById(UUID conversationId);
	
	UUID findDirectConversationIdBetweenUsers( UUID currentUserId,
											   UUID targetUserId);
	
	int updateLastMessage( UUID conversationId,
						   UUID lastMessageId);
	
	List<ConversationSummaryResponse> findConversationSummaries(UUID currentUserId);
	
	UUID findAiConversationIdByUserIdAndAssistantCode(UUID currentUserId, String assistantCode);
}
