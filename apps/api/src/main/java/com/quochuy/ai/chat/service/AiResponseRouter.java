package com.quochuy.ai.chat.service;

import com.quochuy.ai.business.AiBusinessContextResolver;
import com.quochuy.ai.business.dto.AiBusinessContext;
import com.quochuy.ai.business.dto.AiBusinessPrompt;
import com.quochuy.ai.cache.model.AiAnswerCache;
import com.quochuy.ai.cache.service.AiAnswerCacheService;
import com.quochuy.ai.chat.dto.AiRoutePlan;
import com.quochuy.ai.chat.enums.AiAnswerSource;
import com.quochuy.ai.faq.FaqMatcherService;
import com.quochuy.ai.faq.dto.FaqMatchResult;
import com.quochuy.ai.general.dto.GeneralPrompt;
import com.quochuy.ai.prompt.AiPromptBuilder;
import com.quochuy.ai.rag.dto.RagPrompt;
import com.quochuy.ai.rag.dto.RagRetrieveResult;
import com.quochuy.ai.rag.enums.RagAssistantCode;
import com.quochuy.ai.rag.ingestion.RagAssistantCodeResolver;
import com.quochuy.ai.rag.retrieval.RagRetriever;
import com.quochuy.chat.conversation.model.Conversation;
import com.quochuy.chat.message.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Central decision point for AI answer source selection.
 *
 * Keep source priority here so the rest of the AI domain stays simple:
 * deterministic sources first, expensive or less certain LLM paths later.
 *
 * Điều phối cách AI trả lời một tin nhắn.
 *
 * Flow:
 * 1. Thử FAQ/rule-based trước.
 * 2. Nếu có cache câu hỏi giống trước đó thì trả cache.
 * 3. Nếu câu hỏi liên quan dữ liệu nghiệp vụ cửa hàng thì dùng business context.
 * 4. Nếu có tài liệu RAG liên quan thì build prompt từ document context.
 * 5. Nếu không match gì thì trả lời general bằng LLM.
 *
 * Class này chỉ quyết định "đi đường nào", không trực tiếp lưu message.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class AiResponseRouter {
	
	private final FaqMatcherService faqMatcherService;
	private final AiAnswerCacheService aiAnswerCacheService;
	private final AiBusinessContextResolver aiBusinessContextResolver;
	private final RagRetriever ragRetriever;
	private final AiPromptBuilder aiPromptBuilder;
	private final RagAssistantCodeResolver ragAssistantCodeResolver;
	
	public AiRoutePlan routePlan(
			Conversation conversation,
			Message latestUserMessage,
			List<Message> recentMessages
	) {
		String question = latestUserMessage.getContent();
		String assistantCode = conversation.getAssistantCode();
		
		// Priority 1: deterministic FAQ answers are cheapest and most stable.
		// 1) FAQ / rule
		FaqMatchResult faqMatch = faqMatcherService.match(question);
		if (faqMatch.isMatched()) {
			log.info("AI router -> FAQ, intentCode={}", faqMatch.getIntentCode());
			return AiRoutePlan.builder()
					.directContent(faqMatch.getAnswerText())
					.source(AiAnswerSource.FAQ)
					.provider("internal-faq")
					.model("rule-based")
					.promptTokens(null)
					.completionTokens(null)
					.totalTokens(null)
					.latencyMs(0L)
					.cacheable(false)
					.build();
		}
		
		// Priority 2: reuse exact normalized answers before calling any model.
		// 2) Cache
		AiAnswerCache cache = aiAnswerCacheService.findReusableAnswer(assistantCode, question);
		if (cache != null) {
			log.info("AI router -> CACHE, cacheId={}", cache.getCacheId());
			return AiRoutePlan.builder()
					.directContent(cache.getAnswerText())
					.source(AiAnswerSource.CACHE)
					.provider("internal-cache")
					.model("exact-question-cache")
					.promptTokens(null)
					.completionTokens(null)
					.totalTokens(null)
					.latencyMs(0L)
					.cacheable(false)
					.build();
		}
		
		// Priority 3: business facts should beat generic RAG when available.
		// 3) BUSINESS
		AiBusinessContext context = aiBusinessContextResolver.resolve(conversation,
																	  recentMessages);
		if (hasBusinessFacts(context)) {
			log.info("AI router -> BUSINESS, questionType={}", context.getQuestionType());
			
			AiBusinessPrompt prompt = aiPromptBuilder.buildBusinessPrompt(
					conversation,
					recentMessages,
					context
			);
			
			return promptPlan(
					prompt.systemPrompt(),
					prompt.userPrompt(),
					AiAnswerSource.BUSINESS,
					"business-chat-model",
					false
			);
		}
		
		// Priority 4: retrieve document context, then ask the LLM to answer from it.
		// 4) RAG
		RagAssistantCode assistantCodeRag = ragAssistantCodeResolver.resolveAssistantCode(question, recentMessages);
		RagRetrieveResult rag = ragRetriever.retrieve(assistantCodeRag, question);
		if (rag.isMatched()) {
			log.info("AI router -> RAG, topScore={}", rag.getScore());
			
			RagPrompt prompt = aiPromptBuilder.buildRagPrompt(
					conversation,
					recentMessages,
					rag
			);
			
			return promptPlan(
					prompt.systemPrompt(),
					prompt.userPrompt(),
					AiAnswerSource.RAG,
					"rag-chat-model",
					false
			);
		}
		
		// Priority 5: final fallback for questions with no deterministic context.
		// 5) GENERAL fallback
		log.info("AI router -> GENERAL fallback");
		
		GeneralPrompt prompt = aiPromptBuilder.buildGeneralPrompt(
				conversation,
				recentMessages
		);
		
		return promptPlan(
				prompt.systemPrompt(),
				prompt.userPrompt(),
				AiAnswerSource.AI,
				"gemini-2.5-flash",
				true
		);
	}
	
	private boolean hasBusinessFacts(AiBusinessContext context) {
		return context != null && context.isHasFacts();
//				&& context.getFacts() != null
//				&& !context.getFacts().isEmpty();
	}
	
	private AiRoutePlan promptPlan(
			String systemPrompt,
			String userPrompt,
			AiAnswerSource source,
			String model,
			boolean cacheableCandidate
	) {
		return AiRoutePlan.builder()
				.systemPrompt(systemPrompt)
				.userPrompt(userPrompt)
				.source(source)
				.provider("spring-ai")
				.model(model)
				.cacheable(false)
				.cacheableCandidate(cacheableCandidate)
				.build();
	}
}
