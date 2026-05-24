package com.quochuy.ai.embedding;

import java.util.ArrayList;
import java.util.List;

/**
 * Service tạo embedding vector từ text.
 *
 * Dùng chung cho:
 * - RAG ingestion: tạo embedding cho từng document chunk.
 * - RAG retrieval: tạo embedding cho câu hỏi của user.
 *
 * float[] là dạng chính để lưu vector.
 * List<Float> chỉ là dạng phụ để truyền vào query/search mapper.
 */
public interface EmbeddingService {
	float[] embed(String text);
	
	default List<Float> embedAsList(String text) {
		float[] vector = embed(text);
		if (vector == null || vector.length == 0) {
			return List.of();
		}
		List<Float> result = new ArrayList<>(vector.length);
		for (float value : vector) {
			result.add(value);
		}
		return result;
	}
}

