package com.quochuy.chat.conversation.mapper;

import com.quochuy.chat.conversation.model.ConversationParticipant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ConversationParticipantMapper {
	int insertParticipant(ConversationParticipant participant);
	
	boolean existsParticipant(@Param("conversationId") UUID conversationId,
							  @Param("userId") UUID userId);
	
	List<UUID> findUserIdsByConversationId(@Param("conversationId") UUID conversationId);
	
	int updateLastReadMessageId(@Param("conversationId") UUID conversationId,
								@Param("userId") UUID userId,
								@Param("lastReadMessageId") UUID lastReadMessageId);
}
