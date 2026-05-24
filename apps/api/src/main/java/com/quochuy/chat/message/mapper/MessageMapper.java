package com.quochuy.chat.message.mapper;

import com.quochuy.chat.message.model.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface MessageMapper {
	int insertMessage(Message message);
	/**
	 * @return trả về tin nhắn theo Id*/
	Message findById(UUID messageId);
	
	/**
	 * @return lấy ra 20 tin nhắn gần nhất từ chat*/
	List<Message> findRecentMessages(UUID conversationId, int limit);
	
	/**
	 * @return lấy ra 20 tin nhắn gần nhất từ chat*/
	List<Message> findRecentUserMessages(UUID conversationId, int limit);
	
	Message findBySenderAndClientMessageId(UUID senderUserId, String clientMessageId);
	
	List<Message> findMessagesByConversationId(UUID conversationId, UUID beforeMessageId,
											   Integer limit);
}
