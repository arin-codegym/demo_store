package com.quochuy.ai.config;

import com.quochuy.ai.business.BusinessLlmAnswerGenerator;
import com.quochuy.ai.general.GeneralAnswerGenerator;
import com.quochuy.ai.general.GeneralLlmAnswerGenerator;
import com.quochuy.ai.rag.answer.RagAnswerGenerator;
import com.quochuy.ai.rag.answer.RagLlmAnswerGenerator;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {
	
//	@Bean
//	public AiPromptBuilder aiPromptBuilder() {
//		return new DefaultAiPromptBuilder();
//	}
	
//	@Bean
//	public RagRetriever ragRetriever(EmbeddingService embeddingProvider,
//									 RagDocumentSearcher ragDocumentSearcher) {
//		return new HybridRagRetriever(embeddingProvider, ragDocumentSearcher);
//	}
	
	@Bean
	public BusinessLlmAnswerGenerator businessAnswerGenerator(ChatModel chatModel) {
		return new BusinessLlmAnswerGenerator(chatModel);
	}
	
	@Bean
	public RagAnswerGenerator ragAnswerGenerator(ChatModel chatModel) {
		return new RagLlmAnswerGenerator(chatModel);
	}
	
	@Bean
	public GeneralAnswerGenerator generalAnswerGenerator(ChatModel chatModel) {
		return new GeneralLlmAnswerGenerator(chatModel);
	}
	
}
