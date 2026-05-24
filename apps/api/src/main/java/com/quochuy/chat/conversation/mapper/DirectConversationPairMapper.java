package com.quochuy.chat.conversation.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.UUID;

@Mapper
public interface DirectConversationPairMapper {
	UUID findConversationIdByPair(UUID user1Id, UUID user2Id);
	
	void insertPair(UUID conversationId, UUID user1Id, UUID user2Id);
	
	int deleteByConversationId(UUID conversationId);
}
