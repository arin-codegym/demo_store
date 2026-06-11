package com.quochuy.ai.chat.service;

import com.quochuy.ai.cache.service.AiAnswerCacheService;
import com.quochuy.ai.chat.dto.AiRoutePlan;
import com.quochuy.ai.chat.enums.AiAnswerSource;
import com.quochuy.ai.chat.mapper.AiMessageUsageMapper;
import com.quochuy.ai.chat.model.AiMessageUsage;
import com.quochuy.ai.product.ProductComparisonService;
import com.quochuy.ai.product.dto.InternalProductContext;
import com.quochuy.ai.product.dto.ProductComparisonContext;
import com.quochuy.ai.product.dto.ProductComparisonPrompt;
import com.quochuy.ai.prompt.AiPromptBuilder;
import com.quochuy.ai.shared.dto.AiGenerateResult;
import com.quochuy.chat.conversation.mapper.ConversationMapper;
import com.quochuy.chat.conversation.model.Conversation;
import com.quochuy.chat.message.mapper.MessageMapper;
import com.quochuy.chat.message.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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
	private final StreamingLlmAnswerGenerator streamingLlmAnswerGenerator;
	private final AiStreamPublisher aiStreamPublisher;
	private final ProductComparisonService productComparisonService;
	private final AiPromptBuilder aiPromptBuilder;
	
	@Async
	public void generateReply(UUID conversationId, UUID userMessageId) {
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
		
		List<Message> recentMessages = messageMapper.findRecentMessages(conversationId, 20);
		try {
			if (productComparisonService.isProductComparisonQuestion(userMessage.getContent())) {
				handleProductComparison(conversation, userMessage, recentMessages);
				return;
			}
			
			AiRoutePlan plan = aiResponseRouter.routePlan(conversation, userMessage, recentMessages);
			if (plan.isDirect()) {
				persistDirectReply(conversation, userMessage, plan);
				return;
			}
			
			streamAndPersistReply(conversation, userMessage, plan, null);
		} catch (Exception ex) {
			log.error("AI generate failed, conversationId={}, userMessageId={}", conversationId,
					  userMessageId, ex);
			recordFailure(conversationId, userMessageId, ex);
		}
	}
	
	private void handleProductComparison(
			Conversation conversation,
			Message userMessage,
			List<Message> recentMessages
	) {
		String streamId = UUID.randomUUID().toString();
		InternalProductContext internalProduct = productComparisonService.resolveInternalProduct(
				userMessage.getContent());
		String progressContent = productComparisonService.buildInternalProgressMessage(internalProduct);
		aiStreamPublisher.publishDelta(
				conversation.getConversationId(),
				streamId,
				progressContent,
				progressContent,
				AiAnswerSource.PRODUCT_COMPARE
		);
		
		ProductComparisonContext context = productComparisonService.resolve(
				userMessage.getContent(),
				internalProduct
		);
		ProductComparisonPrompt prompt = aiPromptBuilder.buildProductComparisonPrompt(
				conversation,
				recentMessages,
				context
		);
		AiRoutePlan plan = AiRoutePlan.builder()
				.source(AiAnswerSource.PRODUCT_COMPARE)
				.systemPrompt(prompt.systemPrompt())
				.userPrompt(prompt.userPrompt())
				.provider("spring-ai")
				.model("gemini-2.5-flash")
				.cacheable(false)
				.build();
		
		streamAndPersistReply(conversation, userMessage, plan, progressContent, streamId);
	}
	
	private void persistDirectReply(
			Conversation conversation,
			Message userMessage,
			AiRoutePlan plan
	) {
		String content = normalizeAiContent(plan.getDirectContent());
		UUID assistantMessageId = aiMessageCommandService.createAiMessage(
				conversation.getConversationId(),
				content
		);
		recordUsage(conversation, userMessage, assistantMessageId, plan, content);
	}
	
	private void streamAndPersistReply(
			Conversation conversation,
			Message userMessage,
			AiRoutePlan plan,
			String initialStreamContent
	) {
		streamAndPersistReply(conversation, userMessage, plan, initialStreamContent,
							  UUID.randomUUID().toString());
	}
	
	private void streamAndPersistReply(
			Conversation conversation,
			Message userMessage,
			AiRoutePlan plan,
			String initialStreamContent,
			String streamId
	) {
		StringBuilder visibleContent = new StringBuilder(
				initialStreamContent == null ? "" : initialStreamContent
		);
		
		AiGenerateResult result;
		try {
			result = streamingLlmAnswerGenerator.generate(
					plan.getSystemPrompt(),
					plan.getUserPrompt(),
					plan.getModel(),
					delta -> {
						visibleContent.append(delta);
						aiStreamPublisher.publishDelta(
								conversation.getConversationId(),
								streamId,
								delta,
								visibleContent.toString(),
								plan.getSource()
						);
					}
			);
		} catch (Exception ex) {
			String fallbackContent = normalizeAiContent(null);
			aiStreamPublisher.publishDelta(
					conversation.getConversationId(),
					streamId,
					fallbackContent,
					fallbackContent,
					plan.getSource()
			);
			UUID fallbackMessageId = aiMessageCommandService.createAiMessage(
					conversation.getConversationId(),
					fallbackContent
			);
			aiStreamPublisher.publishDone(
					conversation.getConversationId(),
					streamId,
					fallbackMessageId,
					fallbackContent,
					plan.getSource()
			);
			throw ex;
		}
		
		String content = normalizeAiContent(result.getContent());
		UUID assistantMessageId = aiMessageCommandService.createAiMessage(
				conversation.getConversationId(),
				content
		);
		aiStreamPublisher.publishDone(
				conversation.getConversationId(),
				streamId,
				assistantMessageId,
				content,
				plan.getSource()
		);
		recordUsage(conversation, userMessage, assistantMessageId, plan, result, content);
	}
	
	private void recordUsage(
			Conversation conversation,
			Message userMessage,
			UUID assistantMessageId,
			AiRoutePlan plan,
			String content
	) {
		recordUsage(conversation, userMessage, assistantMessageId, plan,
					AiGenerateResult.builder()
							.provider(plan.getProvider())
							.model(plan.getModel())
							.promptTokens(plan.getPromptTokens())
							.completionTokens(plan.getCompletionTokens())
							.totalTokens(plan.getTotalTokens())
							.latencyMs(plan.getLatencyMs())
							.content(content)
							.build(),
					content);
	}
	
	private void recordUsage(
			Conversation conversation,
			Message userMessage,
			UUID assistantMessageId,
			AiRoutePlan plan,
			AiGenerateResult result,
			String content
	) {
		boolean cacheable = plan.isCacheable()
				|| (plan.isCacheableCandidate()
				&& aiAnswerCacheService.shouldCache(userMessage.getContent(), content));
		if (plan.getSource() == AiAnswerSource.AI && cacheable) {
			aiAnswerCacheService.saveReusableAnswer(
					conversation.getAssistantCode(),
					userMessage.getContent(),
					content,
					plan.getSource().name()
			);
		}
		
		aiMessageUsageMapper.insert(AiMessageUsage.builder().usageId(UUID.randomUUID())
				.conversationId(conversation.getConversationId())
				.userMessageId(userMessage.getMessageId())
				.assistantMessageId(assistantMessageId)
				.provider(firstNonBlank(result.getProvider(), plan.getProvider()))
				.model(firstNonBlank(result.getModel(), plan.getModel()))
				.promptTokens(result.getPromptTokens())
				.completionTokens(result.getCompletionTokens())
				.totalTokens(result.getTotalTokens())
				.latencyMs(result.getLatencyMs())
				.status("SUCCESS")
				.errorMessage(null)
				.createdAt(OffsetDateTime.now())
				.build());
	}
	
	private void recordFailure(UUID conversationId, UUID userMessageId, Exception ex) {
		aiMessageUsageMapper.insert(AiMessageUsage.builder().usageId(UUID.randomUUID())
				.conversationId(conversationId)
				.userMessageId(userMessageId)
				.assistantMessageId(null)
				.provider("spring-ai")
				.model("gemini-2.5-flash")
				.promptTokens(null)
				.completionTokens(null)
				.totalTokens(null)
				.latencyMs(null)
				.status("FAILED")
				.errorMessage(ex.getMessage())
				.createdAt(OffsetDateTime.now())
				.build());
	}
	
	private String normalizeAiContent(String content) {
		if (content == null || content.isBlank()) {
			return "Xin lỗi, tôi chưa thể trả lời lúc này.";
		}
		return content.trim();
	}
	
	private String firstNonBlank(String first, String second) {
		if (first != null && !first.isBlank()) {
			return first;
		}
		return second;
	}
}
