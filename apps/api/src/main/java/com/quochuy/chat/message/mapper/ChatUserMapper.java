package com.quochuy.chat.message.mapper;

import com.quochuy.chat.conversation.dto.ConversationSummaryResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ChatUserMapper {
	List<ConversationSummaryResponse> selectChatTableUsers(UUID currentUserId, String keyword);
}
