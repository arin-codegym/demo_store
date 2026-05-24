package com.quochuy.ai.rag.mapper;

import com.quochuy.ai.rag.model.RagImportJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RagImportJobMapper {
	void insert(RagImportJob entity);
	
	RagImportJob findById(@Param("id") Long id);
	
	int markProcessing(@Param("id") Long id);
	
	int markCompleted(@Param("id") Long id, @Param("successDocuments") int successDocuments,
					  @Param("failedDocuments") int failedDocuments);
	
	int markFailed(@Param("id") Long id, @Param("failedDocuments") int failedDocuments,
				   @Param("errorMessage") String errorMessage);
}
