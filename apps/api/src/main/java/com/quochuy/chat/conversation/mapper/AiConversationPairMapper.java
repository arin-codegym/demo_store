package com.quochuy.chat.conversation.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface AiConversationPairMapper {
	UUID upsertPair(@Param("conversationId") UUID conversationId,
					@Param("userId") UUID userId,
					@Param("assistantCode") String assistantCode);
}
