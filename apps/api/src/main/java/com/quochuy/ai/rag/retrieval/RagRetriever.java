package com.quochuy.ai.rag.retrieval;

import com.quochuy.ai.rag.enums.RagAssistantCode;
import com.quochuy.ai.rag.dto.RagRetrieveResult;

public interface RagRetriever {
	/*
	* @Param
	* */
	RagRetrieveResult retrieve(RagAssistantCode assistantCode, String question);
}
