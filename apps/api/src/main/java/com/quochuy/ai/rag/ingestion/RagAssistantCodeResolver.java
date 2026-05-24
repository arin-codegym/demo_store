package com.quochuy.ai.rag.ingestion;

import com.quochuy.ai.rag.enums.RagAssistantCode;
import com.quochuy.chat.message.model.Message;

import java.util.List;

/**
 * Chooses which RAG document namespace should answer a question.
 */
public interface RagAssistantCodeResolver {
	RagAssistantCode resolveAssistantCode(String question, List<Message> recentMessages);
}
