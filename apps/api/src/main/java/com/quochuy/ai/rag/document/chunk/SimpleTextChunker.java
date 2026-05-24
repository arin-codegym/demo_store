package com.quochuy.ai.rag.document.chunk;

import com.quochuy.ai.rag.document.dto.TextChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SimpleTextChunker implements TextChunker {
	
	private static final int MAX_CHARS = 1000;
	private static final int OVERLAP_CHARS = 200;
	
	@Override
	public List<TextChunk> chunk(String text) {
		if (text == null || text.isBlank()) {
			return List.of();
		}
		
		String normalized = normalize(text);
		List<TextChunk> result = new ArrayList<>();
		
		int start = 0;
		while (start < normalized.length()) {
			int end = Math.min(start + MAX_CHARS, normalized.length());
			String chunkText = normalized.substring(start, end).trim();
			
			if (!chunkText.isBlank()) {
				TextChunk chunk = new TextChunk();
				chunk.setText(chunkText);
				chunk.setEstimatedTokenCount(estimateTokens(chunkText));
				result.add(chunk);
			}
			
			if (end == normalized.length()) {
				break;
			}
			
			start = Math.max(0, end - OVERLAP_CHARS);
		}
		
		return result;
	}
	
	private String normalize(String text) {
		return text.replace("\r\n", "\n").trim();
	}
	
	private int estimateTokens(String text) {
		return Math.max(1, text.length() / 4);
	}
}
