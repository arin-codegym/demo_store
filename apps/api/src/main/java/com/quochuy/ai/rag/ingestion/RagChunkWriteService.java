package com.quochuy.ai.rag.ingestion;

import com.quochuy.ai.rag.mapper.RagChunkMapper;
import com.quochuy.ai.rag.model.RagChunkEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RagChunkWriteService {
	private final RagChunkMapper ragChunkMapper;
	
	@Transactional
	public void replaceDocumentChunks(Long documentId, List<RagChunkEntity> chunks) {
		ragChunkMapper.deleteByDocumentId(documentId);
		if (chunks != null && !chunks.isEmpty()) {
			ragChunkMapper.batchInsert(chunks);
		}
	}
}
