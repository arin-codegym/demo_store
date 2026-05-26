package com.quochuy.chat.conversation.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface DirectConversationPairMapper {
	UUID findConversationIdByPair(@Param("user1Id") UUID user1Id,
								  @Param("user2Id") UUID user2Id);
	
	UUID upsertPair(@Param("conversationId") UUID conversationId,
					@Param("user1Id") UUID user1Id,
					@Param("user2Id") UUID user2Id);
	
	int deleteByConversationId(@Param("conversationId") UUID conversationId);
}
