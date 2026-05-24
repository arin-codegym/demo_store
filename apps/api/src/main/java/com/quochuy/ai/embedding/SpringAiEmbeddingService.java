package com.quochuy.ai.embedding;

import com.quochuy.ai.rag.exception.RagImportException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SpringAiEmbeddingService implements EmbeddingService {
	private final EmbeddingModel embeddingModel;
	@Override
	public float[] embed(String text) {
		EmbeddingResponse response = embeddingModel.call(
				new EmbeddingRequest(List.of(text), EmbeddingOptions.builder().build())
		);
		
		if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
			throw new RagImportException("EMBEDDING_EMPTY", "Embedding model returned empty result");
		}
		
		return response.getResults().get(0).getOutput();
	}
	
//	@Override
//	public float[] embed(String text) {
//		if (text == null || text.isBlank()) {
//			return new float[0];
//		}
//
//		float[] output = embeddingModel.embed(text);
//
//		if (output == null || output.length == 0) {
//			throw new RagImportException(
//					"EMBEDDING_EMPTY",
//					"Embedding model returned empty result"
//			);
//		}
//
//		return output;
//	}
}
