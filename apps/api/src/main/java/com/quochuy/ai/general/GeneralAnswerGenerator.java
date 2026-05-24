package com.quochuy.ai.general;

import com.quochuy.ai.shared.dto.AiGenerateResult;
import com.quochuy.ai.general.dto.GeneralPrompt;

public interface GeneralAnswerGenerator {
	AiGenerateResult generate(GeneralPrompt prompt);
}
