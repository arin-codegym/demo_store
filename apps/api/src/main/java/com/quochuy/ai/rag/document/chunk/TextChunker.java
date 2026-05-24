package com.quochuy.ai.rag.document.chunk;

import com.quochuy.ai.rag.document.dto.TextChunk;

import java.util.List;

/**
 * Splits extracted document text into chunks that can be embedded and searched.
 */
public interface TextChunker {
	List<TextChunk> chunk(String text);
}
