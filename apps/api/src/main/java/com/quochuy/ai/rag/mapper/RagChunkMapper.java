package com.quochuy.ai.rag.mapper;

import com.quochuy.ai.rag.model.RagChunkEntity;
import com.quochuy.ai.rag.dto.RagChunkSearchRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RagChunkMapper {
	int deleteByDocumentId(@Param("documentId") Long documentId);
	
	void batchInsert(@Param("items") List<RagChunkEntity> items);
	
	List<RagChunkSearchRow> searchTopChunks(String assistantCode, String queryVector, int topK);
	List<RagChunkSearchRow> searchTopChunksByTsv(String assistantCode, String keyword, int topK);
}
