package com.quochuy.ai.cache.mapper;

import com.quochuy.ai.cache.model.AiAnswerCache;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AiAnswerCacheMapper {
	AiAnswerCache findReusableAnswer(@Param("assistantCode") String assistantCode,
									 @Param("normalizedQuestion") String normalizedQuestion);
	
	void insert(AiAnswerCache cache);
	
	void increaseHitCount(@Param("cacheId") java.util.UUID cacheId,
						  @Param("lastUsedAt") java.time.OffsetDateTime lastUsedAt);
}
