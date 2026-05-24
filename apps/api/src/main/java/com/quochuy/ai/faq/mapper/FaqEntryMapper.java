package com.quochuy.ai.faq.mapper;

import com.quochuy.ai.faq.model.FaqEntry;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FaqEntryMapper {
	List<FaqEntry> findAllEnabled();
}
