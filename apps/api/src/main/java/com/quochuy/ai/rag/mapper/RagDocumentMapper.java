package com.quochuy.ai.rag.mapper;

import com.quochuy.ai.rag.model.RagDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RagDocumentMapper {
	void insert(RagDocument entity);
	
	RagDocument findById(@Param("id") Long id);
	
	int updateImportSuccess(@Param("id") Long id,
							@Param("status") String status,
							@Param("rawText") String rawText,
							@Param("pageCount") Integer pageCount,
							@Param("metadataJson") String metadataJson,
							@Param("updatedBy") String updatedBy);
	
	int updateStatus(@Param("id") Long id,
					 @Param("status") String status);
	
//	RagDocument findById(@Param("id") Long id);
}
