package com.quochuy.ai.business.dto;

import com.quochuy.ai.business.enums.AiQuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiBusinessContext {
	private AiQuestionType questionType;
	private String latestQuestion;
	private List<String> facts;
	private boolean hasFacts; // TO DO
}
