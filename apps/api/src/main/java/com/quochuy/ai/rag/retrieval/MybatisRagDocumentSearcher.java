package com.quochuy.ai.rag.retrieval;

import com.quochuy.ai.rag.mapper.RagChunkMapper;
import com.quochuy.ai.rag.dto.RagChunkSearchRow;
import com.quochuy.ai.rag.dto.RagChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MybatisRagDocumentSearcher implements RagDocumentSearcher {
	
	private final RagChunkMapper ragChunkMapper;
	
	@Override
	public List<RagChunk> searchByVector(String assistantCode, List<Float> queryEmbedding, int topK) {
//		String embeddingJson = toJsonArray(queryEmbedding);
		String queryVector  = toPgVectorLiteral(queryEmbedding);
		List<RagChunkSearchRow> rows = ragChunkMapper.searchTopChunks(
				assistantCode,
				queryVector ,
				topK
		);
		
		if (rows == null || rows.isEmpty()) {
			return List.of();
		}
		
		return rows.stream()
				.map(row -> RagChunk.builder()
						.documentId(row.getDocumentId())
						.chunkId(row.getChunkId())
						.sourceTitle(row.getSourceTitle())
						.content(row.getContent())
						.score(row.getScore())
						.build())
				.toList();
	}
	
	@Override
	public List<RagChunk> searchByKeyword(String assistantCode, String queryText, int topK) {
		List<RagChunkSearchRow> rows = ragChunkMapper.searchTopChunksByTsv(
				assistantCode,
				queryText,
				topK
		);
		
		if (rows == null || rows.isEmpty()) {
			return List.of();
		}
		
		return rows.stream()
				.map(row -> RagChunk.builder()
						.documentId(row.getDocumentId())
						.chunkId(row.getChunkId())
						.sourceTitle(row.getSourceTitle())
						.content(row.getContent())
						.score(row.getScore())
						.build())
				.toList();
	}
	
	private String toJsonArray(List<Float> embedding) {
		if (embedding == null || embedding.isEmpty()) {
			return "[]";
		}
		return embedding.stream()
				.map(String::valueOf)
				.reduce((a, b) -> a + "," + b)
				.map(s -> "[" + s + "]")
				.orElse("[]");
	}
	private RagChunk toChunk(RagChunkSearchRow row) {
		RagChunk chunk =  RagChunk.builder().build();
		chunk.setDocumentId(row.getDocumentId());
		chunk.setChunkId(row.getChunkId());
		chunk.setSourceTitle(row.getSourceTitle());
		chunk.setContent(row.getContent());
		chunk.setScore(row.getScore());
		return chunk;
	}
	
	private String toPgVectorLiteral(List<Float> embedding) {
		return embedding.stream()
				.map(String::valueOf)
				.collect(Collectors.joining(",", "[", "]"));
	}
}
