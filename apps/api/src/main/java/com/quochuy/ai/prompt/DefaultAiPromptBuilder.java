package com.quochuy.ai.prompt;

import com.quochuy.ai.business.dto.AiBusinessContext;
import com.quochuy.ai.business.dto.AiBusinessPrompt;
import com.quochuy.ai.general.dto.GeneralPrompt;
import com.quochuy.ai.rag.dto.RagChunk;
import com.quochuy.ai.rag.dto.RagPrompt;
import com.quochuy.ai.rag.dto.RagRetrieveResult;
import com.quochuy.chat.message.enums.MessageSenderType;
import com.quochuy.chat.conversation.model.Conversation;
import com.quochuy.chat.message.model.Message;
import org.springframework.stereotype.Component;

import java.util.List;
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
