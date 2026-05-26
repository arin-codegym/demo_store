package com.quochuy.chat.message.mapper;

import com.quochuy.chat.message.model.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface MessageMapper {
	int insertMessage(Message message);
	
	Message upsertUserMessage(Message message);
	/**
	 * @return trả về tin nhắn theo Id*/
	Message findById(@Param("messageId") UUID messageId);
	
	/**
	 * @return lấy ra 20 tin nhắn gần nhất từ chat*/
	List<Message> findRecentMessages(@Param("conversationId") UUID conversationId,
									 @Param("limit") int limit);
	
	/**
	 * @return lấy ra 20 tin nhắn gần nhất từ chat*/
	List<Message> findRecentUserMessages(@Param("conversationId") UUID conversationId,
										 @Param("limit") int limit);
	
	Message findBySenderAndClientMessageId(@Param("senderUserId") UUID senderUserId,
										   @Param("clientMessageId") String clientMessageId);
	
	List<Message> findMessagesByConversationId(@Param("conversationId") UUID conversationId,
											   @Param("beforeMessageId") UUID beforeMessageId,
											   @Param("limit") Integer limit);
}
