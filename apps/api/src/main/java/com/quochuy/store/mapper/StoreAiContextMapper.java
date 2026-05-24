package com.quochuy.store.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StoreAiContextMapper {
	
	String findOpeningHoursByAssistantCode(@Param("assistantCode") String assistantCode);
	
	String findHotlineByAssistantCode(@Param("assistantCode") String assistantCode);
	
	String findAddressByAssistantCode(@Param("assistantCode") String assistantCode);
	
	String findReturnPolicyByAssistantCode(@Param("assistantCode") String assistantCode);
}
