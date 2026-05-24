package com.quochuy.chat.conversation.mapper;

import com.quochuy.chat.conversation.model.ConversationParticipant;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ConversationParticipantMapper {
	int insertParticipant(ConversationParticipant participant);
	
	boolean existsParticipant( UUID conversationId,
						   UUID userId);
	
	List<UUID> findUserIdsByConversationId(UUID conversationId);
	
	int updateLastReadMessageId( UUID conversationId,
								 UUID userId,
								 UUID lastReadMessageId);
}
