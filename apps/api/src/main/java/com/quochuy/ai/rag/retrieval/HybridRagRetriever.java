package com.quochuy.ai.rag.retrieval;

import com.quochuy.ai.rag.enums.RagAssistantCode;
import com.quochuy.ai.rag.dto.RagChunk;
import com.quochuy.ai.rag.dto.RagRetrieveResult;
import com.quochuy.ai.embedding.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Retrieves RAG context with a hybrid vector + keyword strategy.
 *
 * The retriever never calls the LLM. It only decides whether there is enough
 * document context for AiResponseRouter to choose the RAG answer path.
 *
 * Retriever dùng chiến lược hybrid search.
 *
 * Nhiệm vụ:
 * - Embed câu hỏi của user.
 * - Search tài liệu theo vector similarity.
 * - Search thêm theo keyword.
 * - Gộp hai danh sách bằng rank-score:
 *   vector = 70%, keyword = 30%.
 * - Lọc kết quả dưới ngưỡng relevance.
 *
 * Class này chỉ tìm context liên quan, không gọi LLM và không sinh câu trả lời.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class HybridRagRetriever implements RagRetriever {
	private static final int DEFAULT_TOP_K = 5;
	private static final double HYBRID_SCORE_THRESHOLD = 0.45;
	private final EmbeddingService embeddingService;
	private final RagDocumentSearcher ragDocumentSearcher;
	@Value("${spring.ai.embedding.dimensions:1536}")
	private int embeddingDimension;
	public RagRetrieveResult retrieve(RagAssistantCode assistantCode, String question) {
		String normalizedAssistantCode = assistantCode == null ? "" : safe(assistantCode.name());
		String normalizedQuestion = normalizeQuestion(question);
		if (normalizedAssistantCode.isBlank() || normalizedQuestion.isBlank()) {
			log.info("RAG retrieve skipped: blank assistantCode/question");
			return emptyResult();
		}
		List<Float> queryEmbedding = embeddingService.embedAsList(normalizedQuestion);
		if (queryEmbedding == null || queryEmbedding.isEmpty()) {
			log.warn("RAG retrieve skipped: query embedding empty, assistantCode={}",
					 normalizedAssistantCode);
			return emptyResult();
		}
		if (queryEmbedding.size() != embeddingDimension) {
			throw new RuntimeException(
					"Invalid embedding dimension: expected 1536, but got " + queryEmbedding.size());
		}
		List<RagChunk> vectorChunks = ragDocumentSearcher.searchByVector(normalizedAssistantCode,
																		 queryEmbedding,
																		 DEFAULT_TOP_K);
		List<RagChunk> keywordChunks = ragDocumentSearcher.searchByKeyword(normalizedAssistantCode,
																		   normalizedQuestion,
																		   DEFAULT_TOP_K);
		boolean noVectorHit = vectorChunks == null || vectorChunks.isEmpty();
		boolean noKeywordHit = keywordChunks == null || keywordChunks.isEmpty();
		if (noVectorHit && noKeywordHit) {
			log.info("RAG retrieve no hit, assistantCode={}", normalizedAssistantCode);
			return emptyResult();
		}
		List<RagChunk> mergedChunks = mergeHybrid(
				noVectorHit ? Collections.emptyList() : vectorChunks,
				noKeywordHit ? Collections.emptyList() : keywordChunks);
		if (mergedChunks == null || mergedChunks.isEmpty()) {
			log.info("RAG retrieve merged empty, assistantCode={}", normalizedAssistantCode);
			return emptyResult();
		}
		List<RagChunk> matchedChunks = mergedChunks.stream().filter(Objects::nonNull)
//			.filter(chunk -> chunk.getContent() != null && !chunk.getContent().isBlank())
				.filter(chunk -> chunk.getScore() != null && chunk.getScore() >= HYBRID_SCORE_THRESHOLD)
				.toList();
		double topScore = matchedChunks.stream().map(RagChunk::getScore).filter(Objects::nonNull)
				.max(Comparator.naturalOrder()).orElse(0.0d);
		if (matchedChunks.isEmpty()) {
			log.info(
					"RAG retrieve below threshold/empty after filtering, assistantCode={}, topScore={}",
					normalizedAssistantCode, topScore);
			return RagRetrieveResult.builder().matched(false).score(topScore)
					.chunks(Collections.emptyList()).build();
		}
		log.info("RAG retrieve matched, assistantCode={}, chunkCount={}, topScore={}",
				 normalizedAssistantCode, matchedChunks.size(), topScore);
		return RagRetrieveResult.builder().matched(true).score(topScore).chunks(matchedChunks)
				.build();
	}
	
	private List<RagChunk> mergeHybrid(List<RagChunk> vectorChunks, List<RagChunk> keywordChunks) {
		// Convert vector and keyword ranks to the same 0..1 shape before merging.
		Map<String, HybridScore> scoreMap = new LinkedHashMap<>();
		addRankScore(scoreMap, vectorChunks, 0.7, "vector");
		addRankScore(scoreMap, keywordChunks, 0.3, "keyword");
		return scoreMap.values().stream()
				.sorted(Comparator.comparing(HybridScore::getFinalScore).reversed())
				.map(HybridScore::toChunk).limit(5).toList();
	}
	
	private void addRankScore(Map<String, HybridScore> scoreMap, List<RagChunk> chunks,
							  double weight, String source) {
		for (int i = 0; i < chunks.size(); i++) {
			RagChunk chunk = chunks.get(i);
			if (chunk == null || chunk.getChunkId() == null) {
				continue;
			}
			//đưa cả hai về cùng một "ngôn ngữ": Thứ hạng.(vectorChunks,keywordChunks) về hệ số 0~1
			double rankScore = (chunks.size() - i) * 1.0 / chunks.size();
			HybridScore hs = scoreMap.computeIfAbsent(chunk.getChunkId(),
													  id -> new HybridScore(chunk));
			hs.addScore(source, rankScore * weight);
		}
	}
	
	private RagRetrieveResult emptyResult() {
		return RagRetrieveResult.builder().matched(false).score(0.0d)
				.chunks(Collections.emptyList()).build();
	}
	
	private String normalizeQuestion(String question) {
		if (question == null) {
			return "";
		}
		return question.trim().replaceAll("\\s+", " ");
	}
	
	private String safe(String value) {
		return value == null ? "" : value.trim();
	}
	
	static class HybridScore {
		private final RagChunk chunk;
		private double vectorScore;
		private double keywordScore;
		
		public HybridScore(RagChunk chunk) {
			this.chunk = chunk;
		}
		
		public void addScore(String source, double score) {
			if ("vector".equals(source)) {
				this.vectorScore += score;
			} else if ("keyword".equals(source)) {
				this.keywordScore += score;
			}
		}
		
		public double getFinalScore() {
			return vectorScore + keywordScore;
		}
		
		public RagChunk toChunk() {
			return RagChunk.builder().documentId(chunk.getDocumentId()).chunkId(chunk.getChunkId())
					.sourceTitle(chunk.getSourceTitle()).content(chunk.getContent())
					.score(getFinalScore()).build();
		}
	}
}
