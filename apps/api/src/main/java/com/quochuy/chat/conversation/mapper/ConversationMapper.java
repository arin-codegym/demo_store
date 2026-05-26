package com.quochuy.chat.conversation.mapper;

import com.quochuy.chat.conversation.model.Conversation;
import com.quochuy.chat.conversation.dto.ConversationSummaryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ConversationMapper {
	int insertConversation(Conversation conversation);
	
	Conversation findById(@Param("conversationId") UUID conversationId);

	Conversation lockById(@Param("conversationId") UUID conversationId);

	int deleteById(@Param("conversationId") UUID conversationId);
	
	UUID findDirectConversationIdBetweenUsers(@Param("currentUserId") UUID currentUserId,
											  @Param("targetUserId") UUID targetUserId);
	
	int updateLastMessage(@Param("conversationId") UUID conversationId,
						  @Param("lastMessageId") UUID lastMessageId);
	
	List<ConversationSummaryResponse> findConversationSummaries(@Param("currentUserId") UUID currentUserId);
	
	UUID findAiConversationIdByUserIdAndAssistantCode(@Param("currentUserId") UUID currentUserId,
													  @Param("assistantCode") String assistantCode);
}
