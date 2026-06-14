package com.quochuy.ai.prompt;

import com.quochuy.ai.business.dto.AiBusinessContext;
import com.quochuy.ai.business.dto.AiBusinessPrompt;
import com.quochuy.ai.general.dto.GeneralPrompt;
import com.quochuy.ai.product.dto.ExternalProductContext;
import com.quochuy.ai.product.dto.ExternalProductSearchResult;
import com.quochuy.ai.product.dto.InternalProductContext;
import com.quochuy.ai.product.dto.InternalProductFact;
import com.quochuy.ai.product.dto.ProductComparisonContext;
import com.quochuy.ai.product.dto.ProductComparisonPrompt;
import com.quochuy.ai.rag.dto.RagChunk;
import com.quochuy.ai.rag.dto.RagPrompt;
import com.quochuy.ai.rag.dto.RagRetrieveResult;
import com.quochuy.chat.message.enums.MessageSenderType;
import com.quochuy.chat.conversation.model.Conversation;
import com.quochuy.chat.message.model.Message;
import org.springframework.stereotype.Component;

import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringJoiner;

/**
 * Default prompt formatter for the three routed LLM paths: business, RAG, and
 * general fallback.
 */
@Component
public class DefaultAiPromptBuilder implements AiPromptBuilder {
	
	@Override
	public AiBusinessPrompt buildBusinessPrompt(
			Conversation conversation,
			List<Message> recentMessages,
			AiBusinessContext context
	) {
		String systemPrompt = """
                Bạn là trợ lý AI của hệ thống cửa hàng.
                Hãy trả lời bằng tiếng Việt, ngắn gọn, rõ ràng, đúng trọng tâm.
                Chỉ được sử dụng dữ liệu trong BUSINESS_FACTS để trả lời.
                Không được bịa thêm dữ liệu ngoài BUSINESS_FACTS.
                Nếu BUSINESS_FACTS chưa đủ để kết luận chính xác, hãy nói rõ là chưa đủ dữ liệu.
                """;
		
		String historyText = buildHistoryText(recentMessages);
		String factsText = buildFactsText(context.getFacts());
		String latestQuestion = safe(context.getLatestQuestion());
		String questionType = context.getQuestionType() == null
				? "UNKNOWN"
				: context.getQuestionType().name();
		
		String userPrompt = """
                LỊCH SỬ HỘI THOẠI:
                %s

                CÂU HỎI MỚI NHẤT:
                %s

                LOẠI CÂU HỎI:
                %s

                BUSINESS_FACTS:
                %s

                YÊU CẦU:
                - Trả lời tự nhiên bằng tiếng Việt.
                - Chỉ dùng BUSINESS_FACTS.
                - Không bịa thêm thông tin.
                - Nếu dữ liệu chưa đủ thì nói rõ là chưa đủ dữ liệu.
                """.formatted(historyText, latestQuestion, questionType, factsText);
		
		return new AiBusinessPrompt(systemPrompt, userPrompt);
	}
	
	@Override
	public RagPrompt buildRagPrompt(
			Conversation conversation,
			List<Message> recentMessages,
			RagRetrieveResult rag
	) {
		String systemPrompt = """
                Bạn là trợ lý AI của hệ thống cửa hàng.
                Hãy trả lời bằng tiếng Việt, ngắn gọn, rõ ràng.
                Chỉ được sử dụng thông tin trong DOCUMENT_CONTEXT để trả lời.
                Không được bịa thêm thông tin ngoài DOCUMENT_CONTEXT.
                Nếu DOCUMENT_CONTEXT chưa đủ để kết luận, hãy nói rõ là chưa đủ dữ liệu.
                """;
		
		String historyText = buildHistoryText(recentMessages);
		String latestQuestion = extractLatestUserQuestion(recentMessages);
		String docsText = buildRagChunksText(rag.getChunks());
		
		String userPrompt = """
                LỊCH SỬ HỘI THOẠI:
                %s

                CÂU HỎI MỚI NHẤT:
                %s

                DOCUMENT_CONTEXT:
                %s

                YÊU CẦU:
                - Trả lời câu hỏi mới nhất bằng tiếng Việt tự nhiên.
                - Chỉ dùng DOCUMENT_CONTEXT.
                - Không bịa thêm thông tin.
                - Nếu tài liệu chưa đủ thì nói rõ là chưa đủ dữ liệu.
                """.formatted(historyText, latestQuestion, docsText);
		
		return new RagPrompt(systemPrompt, userPrompt);
	}
	
	@Override
	public GeneralPrompt buildGeneralPrompt(
			Conversation conversation,
			List<Message> recentMessages
	) {
		String systemPrompt = """
                Bạn là trợ lý AI trong hệ thống chat của cửa hàng.
                Hãy trả lời bằng tiếng Việt, ngắn gọn, rõ ràng, đúng trọng tâm.
                Nếu không chắc chắn, hãy nói rõ là bạn chưa đủ dữ liệu.
                Không bịa thông tin.
                """;
		
		String historyText = buildHistoryText(recentMessages);
		
		String userPrompt = """
                Dưới đây là lịch sử hội thoại gần nhất.
                Hãy trả lời tin nhắn mới nhất của người dùng một cách tự nhiên.

                %s
                """.formatted(historyText);
		
		return new GeneralPrompt(systemPrompt, userPrompt);
	}
	
	@Override
	public ProductComparisonPrompt buildProductComparisonPrompt(
			Conversation conversation,
			List<Message> recentMessages,
			ProductComparisonContext context
	) {
		String systemPrompt = """
                You are the store AI shopping assistant.
                Answer in Vietnamese, clearly and concisely.
                Compare products only from OWN_PRODUCT_CONTEXT and EXTERNAL_PRODUCT_CONTEXT.
                Do not invent prices, specs, reviews, warranties, or availability.
                External search snippets can be incomplete; mention uncertainty and cite source URLs when using them.
                If either side lacks enough data, say exactly what is missing before giving a cautious recommendation.
                """;
		
		String historyText = buildHistoryText(recentMessages);
		String ownProductsText = buildInternalProductsText(context.internalProduct());
		String externalProductsText = buildExternalProductsText(context.externalProduct());
		
		String userPrompt = """
                RECENT_CHAT_HISTORY:
                %s

                USER_QUESTION:
                %s

                OWN_PRODUCT_CONTEXT:
                %s

                EXTERNAL_PRODUCT_CONTEXT:
                %s

                TASK:
                - First summarize the matching store product facts.
                - Then compare against the external product information.
                - Include a compact comparison table when possible.
                - End with a practical buying recommendation.
                - If external data is missing or search is not configured, do not pretend to know live internet data.
                """.formatted(
				historyText,
				safe(context.question()),
				ownProductsText,
				externalProductsText
		);
		
		return new ProductComparisonPrompt(systemPrompt, userPrompt);
	}
	
	private String buildHistoryText(List<Message> recentMessages) {
		if (recentMessages == null || recentMessages.isEmpty()) {
			return "";
		}
		
		StringJoiner history = new StringJoiner("\n");
		for (Message message : recentMessages) {
			String role = message.getSenderType() == MessageSenderType.AI ? "assistant" : "user";
			history.add(role + ": " + safe(message.getContent()));
		}
		return history.toString();
	}
	
	private String buildFactsText(List<String> facts) {
		if (facts == null || facts.isEmpty()) {
			return "- Không có dữ liệu nghiệp vụ.";
		}
		
		StringJoiner joiner = new StringJoiner("\n");
		for (String fact : facts) {
			joiner.add("- " + safe(fact));
		}
		return joiner.toString();
	}
	
	private String buildRagChunksText(List<RagChunk> chunks) {
		if (chunks == null || chunks.isEmpty()) {
			return "- Không có tài liệu liên quan.";
		}
		
		StringJoiner joiner = new StringJoiner("\n\n");
		for (RagChunk chunk : chunks) {
			joiner.add("""
                    Nguồn: %s
                    Điểm liên quan: %s
                    Nội dung: %s
                    """.formatted(
					safe(chunk.getSourceTitle()),
					chunk.getScore() == null ? "" : chunk.getScore(),
					safe(chunk.getContent())
			));
		}
		return joiner.toString();
	}
	
	private String buildInternalProductsText(InternalProductContext context) {
		if (context == null || !context.hasProducts()) {
			return "- No matching store product was found from the catalog search.";
		}
		StringJoiner joiner = new StringJoiner("\n\n");
		for (InternalProductFact product : context.products()) {
			joiner.add("""
                    Product ID: %s
                    Name: %s
                    Company: %s
                    Price: %s
                    Description: %s
                    Image: %s
                    """.formatted(
					product.productId(),
					safe(product.name()),
					safe(product.company()),
					formatPrice(product.price()),
					safe(product.description()),
					safe(product.image())
			));
		}
		return joiner.toString();
	}
	
	private String buildExternalProductsText(ExternalProductContext context) {
		if (context == null) {
			return "- External search was not executed.";
		}
		if (!context.configured()) {
			return "- External search is not available. Provider: %s. Reason: %s"
					.formatted(safe(context.provider()), safe(context.errorMessage()));
		}
		if (!context.hasResults()) {
			return "- External search returned no usable results. Provider: %s. Query: %s"
					.formatted(safe(context.provider()), safe(context.query()));
		}
		StringJoiner joiner = new StringJoiner("\n\n");
		for (ExternalProductSearchResult result : context.results()) {
			joiner.add("""
                    Provider: %s
                    Title: %s
                    Snippet: %s
                    URL: %s
                    Site: %s
                    """.formatted(
					safe(context.provider()),
					safe(result.title()),
					safe(result.snippet()),
					safe(result.url()),
					safe(result.displayLink())
			));
		}
		return joiner.toString();
	}
	
	private String formatPrice(int price) {
		return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(price) + " VND";
	}
	
	private String extractLatestUserQuestion(List<Message> recentMessages) {
		if (recentMessages == null || recentMessages.isEmpty()) {
			return "";
		}
		
		for (int i = recentMessages.size() - 1; i >= 0; i--) {
			Message message = recentMessages.get(i);
			if (message.getSenderType() != MessageSenderType.AI) {
				return safe(message.getContent());
			}
		}
		return "";
	}
	
	private String safe(String value) {
		return value == null ? "" : value.trim();
	}
	
}
