package com.quochuy.chat.message.service;

import com.quochuy.chat.message.mapper.ChatUserMapper;
import com.quochuy.chat.conversation.dto.ConversationSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatUserService {
	private final ChatUserMapper chatUserMapper;
	
	public List<ConversationSummaryResponse> getChatableUsers(UUID currentUserId, String keyword) {
		return chatUserMapper.selectChatTableUsers(currentUserId, keyword);
	}
}
