package com.quochuy.ai.rag.retrieval;

import com.quochuy.ai.rag.dto.RagChunk;

import java.util.List;

public interface RagDocumentSearcher {
	List<RagChunk> searchByVector(String assistantCode, List<Float> queryEmbedding, int topK);
	
	List<RagChunk> searchByKeyword(String normalizedAssistantCode, String normalizedQuestion, int i);
}
