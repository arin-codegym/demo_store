package com.quochuy.ai.chat.service;

import com.quochuy.ai.cache.service.AiAnswerCacheService;
import com.quochuy.ai.chat.enums.AiAnswerSource;
import com.quochuy.ai.chat.dto.AiRoutedResponse;
import com.quochuy.ai.chat.mapper.AiMessageUsageMapper;
import com.quochuy.chat.conversation.mapper.ConversationMapper;
import com.quochuy.chat.message.mapper.MessageMapper;
import com.quochuy.ai.chat.model.AiMessageUsage;
import com.quochuy.chat.conversation.model.Conversation;
import com.quochuy.chat.message.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Orchestrates one AI reply for a USER_AI conversation.
 *
 * This service loads the current message and conversation state, delegates
 * answer-source selection to AiResponseRouter, stores the assistant message,
 * and records usage metadata.
 *
 * Service tạo phản hồi AI cho một conversation.
 *
 * Flow:
 * 1. Lấy message user vừa gửi.
 * 2. Lấy conversation và lịch sử gần nhất.
 * 3. Gọi AiResponseRouter để quyết định FAQ / CACHE / BUSINESS / RAG / GENERAL.
 * 4. Lưu câu trả lời AI thành message mới.
 * 5. Ghi usage log.
 *
 * Method generateReply chạy async sau khi MessageCreatedEvent được commit.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class AiChatService {
	private final ConversationMapper conversationMapper;
	private final MessageMapper messageMapper;
	private final AiMessageCommandService aiMessageCommandService;
	private final AiMessageUsageMapper aiMessageUsageMapper;
	private final AiResponseRouter aiResponseRouter;
	private final AiAnswerCacheService aiAnswerCacheService;
	

	@Async
	public void generateReply(UUID conversationId, UUID userMessageId) {
		// Load persisted state after the message-created transaction has committed.
		/*Lây ra tin nhắn gần nhất => câu hỏi*/
		Message userMessage = messageMapper.findById(userMessageId);
		if (userMessage == null) {
			log.warn("AI skipped: user message not found, messageId={}", userMessageId);
			return;
		}
		Conversation conversation = conversationMapper.findById(conversationId);
		if (conversation == null) {
			log.warn("AI skipped: conversation not found, conversationId={}", conversationId);
			return;
		}
		List<Message> recentMessages = messageMapper.findRecentUserMessages(conversationId, 20);
		UUID assistantMessageId = null;
		try {
			// Route first, then persist the generated assistant message.
			//Following the routing logic to determine which AI provider/model to use and get the response
			AiRoutedResponse routed = aiResponseRouter.route(conversation, userMessage,
															 recentMessages);
			String content = normalizeAiContent(routed.getContent());
			assistantMessageId = aiMessageCommandService.createAiMessage(conversationId, content);
			
			// Cache only generic LLM fallback answers that passed cache-safety checks.
			//CACHE
			if (routed.getSource() == AiAnswerSource.AI && Boolean.TRUE.equals(routed.isCacheable())) {
				aiAnswerCacheService.saveReusableAnswer(
						conversation.getAssistantCode(),
						userMessage.getContent(),
						content,
						routed.getSource().name()
				);
			}
			
			aiMessageUsageMapper.insert(AiMessageUsage.builder().usageId(UUID.randomUUID())
												.conversationId(conversationId)
												.userMessageId(userMessageId)
												.assistantMessageId(assistantMessageId)
												.provider(routed.getProvider())
												.model(routed.getModel())
												.promptTokens(routed.getPromptTokens())
												.completionTokens(routed.getCompletionTokens())
												.totalTokens(routed.getTotalTokens())
												.latencyMs(routed.getLatencyMs()).status("SUCCESS")
												.errorMessage(null).createdAt(OffsetDateTime.now())
												.build());
		} catch (Exception ex) {
			log.error("AI generate failed, conversationId={}, userMessageId={}", conversationId,
					  userMessageId, ex);
			aiMessageUsageMapper.insert(AiMessageUsage.builder().usageId(UUID.randomUUID())
												.conversationId(conversationId)
												.userMessageId(userMessageId)
												.assistantMessageId(null).provider("google-genai")
												.model("gemini-2.5-flash").promptTokens(null)
												.completionTokens(null).totalTokens(null)
												.latencyMs(null).status("FAILED")
												.errorMessage(ex.getMessage())
												.createdAt(OffsetDateTime.now()).build());
		}
	}
	
	private String normalizeAiContent(String content) {
		if (content == null || content.isBlank()) {
			return "Xin lỗi, tôi chưa thể trả lời lúc này.";
		}
		return content.trim();
	}
}
