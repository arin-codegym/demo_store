package com.quochuy.ai.chat.mapper;

import com.quochuy.ai.chat.model.AiMessageUsage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiMessageUsageMapper {
	void insert(AiMessageUsage usage);
}
