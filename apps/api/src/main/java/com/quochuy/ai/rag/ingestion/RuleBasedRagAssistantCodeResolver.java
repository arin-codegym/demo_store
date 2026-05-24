package com.quochuy.ai.rag.ingestion;

import com.quochuy.ai.rag.enums.RagAssistantCode;
import com.quochuy.ai.rag.ingestion.RagAssistantCodeResolver;
import com.quochuy.chat.message.model.Message;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleBasedRagAssistantCodeResolver implements RagAssistantCodeResolver {
	@Override
	public RagAssistantCode resolveAssistantCode(String question, List<Message> recentMessages) {
		String normalizedQuestion = normalize(question);
		
		// 1. Ưu tiên classify trực tiếp từ câu hỏi hiện tại
		RagAssistantCode currentIntent = resolveByQuestion(normalizedQuestion);
		if (!RagAssistantCode.COMMON.equals(currentIntent)) {
			return currentIntent ;
		}
		
		// 2. Nếu câu hỏi hiện tại mơ hồ / phụ thuộc ngữ cảnh thì mới fallback history
		if (isContextDependent(normalizedQuestion)) {
			RagAssistantCode historyIntent = resolveFromHistory(recentMessages);
			if (!RagAssistantCode.COMMON.equals(historyIntent)) {
				return historyIntent;
			}
		}
		
		// 3. Nếu không rõ gì cả thì COMMON
		return RagAssistantCode.COMMON;
	}
	private RagAssistantCode  resolveByQuestion(String text) {
		Score score = scoreText(text, 3); // weight cao hơn history
		return toAssistantCode(score);
	}
	private RagAssistantCode resolveFromHistory(List<Message> recentMessages) {
		if (recentMessages == null || recentMessages.isEmpty()) {
			return RagAssistantCode.COMMON;
		}
		
		Score total = new Score();
		
		// chỉ xét 6 tin gần nhất để giảm nhiễu
		int start = Math.max(0, recentMessages.size() - 6);
		
		for (int i = start; i < recentMessages.size(); i++) {
			Message message = recentMessages.get(i);
			if (message == null || message.getContent() == null) {
				continue;
			}
			
			String content = normalize(message.getContent());
			
			// càng gần hiện tại càng trọng số cao
			int recencyWeight = i - start + 1;
			
			Score messageScore = scoreText(content, recencyWeight );
			total.add(messageScore);
		}
		
		return toAssistantCode(total);
	}
	
	/**
	 * Kiểm tra xem câu hỏi hiện tại có mơ hồ / phụ thuộc context không.
	 */
	private boolean isContextDependent(String question) {
		if (question == null || question.isBlank()) {
			return false;
		}
		
		// quá ngắn thường mơ hồ
		if (question.length() <= 18) {
			return true;
		}
		
		return containsAny(question,
						   "cái đó",
						   "cái này",
						   "nó",
						   "vậy",
						   "thế",
						   "thế còn",
						   "còn cái này",
						   "trường hợp này",
						   "bên trên",
						   "ở trên",
						   "ý là sao",
						   "rồi sao",
						   "tiếp theo",
						   "sao nữa",
						   "áp dụng luôn hả",
						   "được không"
		);
	}
	
	/**
	 * Chấm điểm text cho từng category.
	 */
	private Score scoreText(String text, int weight) {
		Score score = new Score();
		
		// POLICY
		if (containsAny(text,
						"chính sách",
						"đổi trả",
						"trả hàng",
						"bảo hành",
						"hoàn tiền",
						"refund",
						"warranty")) {
			score.policy += weight;
		}
		
		// USER_GUIDE
		if (containsAny(text,
						"hướng dẫn",
						"cách dùng",
						"cách sử dụng",
						"sử dụng",
						"thiết lập",
						"cài đặt",
						"setup",
						"config",
						"hướng dẫn dùng")) {
			score.userGuide += weight;
		}
		
		// FAQ
		if (containsAny(text,
						"faq",
						"câu hỏi thường gặp",
						"thường gặp",
						"hay hỏi")) {
			score.faq += weight;
		}
		
		return score;
	}
	
	private RagAssistantCode  toAssistantCode(Score score) {
		int max = Math.max(score.policy, Math.max(score.userGuide, score.faq));
		
		if (max == 0) {
			return RagAssistantCode.COMMON;
		}
		
		int countMax = 0;
		if (score.policy == max) countMax++;
		if (score.userGuide == max) countMax++;
		if (score.faq == max) countMax++;
		
		if (countMax > 1) {
			return RagAssistantCode.COMMON;
		}
		
		if (score.policy == max) {
			return RagAssistantCode.POLICY;
		}
		if (score.userGuide == max) {
			return RagAssistantCode.USER_GUIDE;
		}
		return RagAssistantCode.FAQ;
	}
	
	private String normalize(String input) {
		return input == null ? "" : input.trim().toLowerCase();
	}
	
	private boolean containsAny(String text, String... keywords) {
		if (text == null || text.isBlank()) {
			return false;
		}
		
		for (String keyword : keywords) {
			if (text.contains(keyword)) {
				return true;
			}
		}
		return false;
	}
	
	private static class Score {
		int policy;
		int userGuide;
		int faq;
		
		void add(Score other) {
			this.policy += other.policy;
			this.userGuide += other.userGuide;
			this.faq += other.faq;
		}
	}
}
