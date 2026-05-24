package com.quochuy.ai.business;

import com.quochuy.ai.business.dto.AiBusinessContext;
import com.quochuy.ai.business.enums.AiQuestionType;
import com.quochuy.chat.message.enums.MessageSenderType;
import com.quochuy.store.mapper.StoreAiContextMapper;
import com.quochuy.chat.conversation.model.Conversation;
import com.quochuy.chat.message.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts a user question into structured business facts for the router.
 *
 * When this resolver returns hasFacts=false, the router continues to RAG and
 * then the general fallback. Keep DB lookups and business-specific enrichment
 * in this class instead of spreading them through the router.
 */
@Component
@RequiredArgsConstructor
public class AiBusinessContextResolver  {
	private final AiQuestionClassifier aiQuestionClassifier;
	private final StoreAiContextMapper storeAiContextMapper;
	public AiBusinessContext resolve(Conversation conversation, List<Message> recentMessages) {
		String latestQuestion = extractLatestUserQuestion(recentMessages);
		AiQuestionType questionType = aiQuestionClassifier.classify(latestQuestion);
		
		List<String> facts = switch (questionType) {
//			case STORE_INFO -> resolveStoreInfo(conversation);
//			case ORDER_STATUS -> resolveOrderInfoPlaceholder(latestQuestion);
//			case PRODUCT_INFO -> resolveProductInfoPlaceholder(latestQuestion);
//			case GENERAL -> resolveGeneralContext(conversation);
			case STORE_INFO -> List.of();
			case ORDER_STATUS -> List.of();
			case PRODUCT_INFO -> List.of();
			case GENERAL -> List.of();
			default -> List.of();
		};
		if(facts.isEmpty()){
			return AiBusinessContext.builder()
					.hasFacts(false)
					.build();
		}
		return AiBusinessContext.builder()
				.hasFacts(true)
				.questionType(questionType)
				.latestQuestion(latestQuestion)
				.facts(facts)
				.build();
	}
	
	private List<String> resolveStoreInfo(Conversation conversation) {
		String assistantCode = conversation.getAssistantCode();
		List<String> facts = new ArrayList<>();
		// example logic thực tế
//		String openingHours = storeAiContextMapper.findOpeningHoursByAssistantCode(assistantCode);
//		String hotline = storeAiContextMapper.findHotlineByAssistantCode(assistantCode);
//		String address = storeAiContextMapper.findAddressByAssistantCode(assistantCode);
//		String returnPolicy = storeAiContextMapper.findReturnPolicyByAssistantCode(assistantCode);
//
//		if (openingHours != null && !openingHours.isBlank()) {
//			facts.add("Giờ mở cửa: " + openingHours);
//		}
//		if (hotline != null && !hotline.isBlank()) {
//			facts.add("Hotline: " + hotline);
//		}
//		if (address != null && !address.isBlank()) {
//			facts.add("Địa chỉ: " + address);
//		}
//		if (returnPolicy != null && !returnPolicy.isBlank()) {
//			facts.add("Chính sách đổi trả: " + returnPolicy);
//		}
//
//		if (facts.isEmpty()) {
//			facts.add("Chưa có dữ liệu cấu hình cửa hàng trong hệ thống.");
//		}
		if ("store-support".equalsIgnoreCase(assistantCode)) {
			facts.add("Giờ mở cửa: 08:00 - 22:00 mỗi ngày");
			facts.add("Hotline: 1900 1234");
			facts.add("Đổi trả trong 7 ngày nếu sản phẩm lỗi");
		}
		return facts;
	}
	
	private List<String> resolveOrderInfoPlaceholder(String latestQuestion) {
		// String orderCode = extractOrderCode(latestQuestion); ví dụ tìm sâu hơn theo câu hỏi
		List<String> facts = new ArrayList<>();
		facts.add("Câu hỏi này thuộc nhóm tra cứu đơn hàng.");
		facts.add("Nếu không có mã đơn hàng hoặc định danh đơn hàng, không được tự suy đoán trạng thái.");
		facts.add("Hiện chưa truy xuất dữ liệu đơn hàng cụ thể từ câu hỏi: " + safe(latestQuestion));
		return facts;
	}
	
	private List<String> resolveProductInfoPlaceholder(String latestQuestion) {
		List<String> facts = new ArrayList<>();
		facts.add("Câu hỏi này thuộc nhóm sản phẩm hoặc tồn kho.");
		facts.add("Nếu chưa có mã sản phẩm hoặc tên sản phẩm rõ ràng, không được tự suy đoán.");
		facts.add("Hiện chưa truy xuất dữ liệu sản phẩm cụ thể từ câu hỏi: " + safe(latestQuestion));
		return facts;
	}
	
	private List<String> resolveGeneralContext(Conversation conversation) {
//		List<String> facts = new ArrayList<>();
//		facts.add("Assistant code: " + safe(conversation.getAssistantCode()));
//		facts.add("Đây là câu hỏi hội thoại chung, không cần truy xuất dữ liệu nghiệp vụ từ DB.");
//		facts.add("Chỉ trả lời dựa trên lịch sử hội thoại và hướng dẫn hệ thống.");
//		return facts;
		return List.of();
	}
	
	private String extractLatestUserQuestion(List<Message> recentMessages) {
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
