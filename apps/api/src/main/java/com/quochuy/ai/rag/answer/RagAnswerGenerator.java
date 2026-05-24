package com.quochuy.ai.rag.answer;

import com.quochuy.ai.shared.dto.AiGenerateResult;
import com.quochuy.ai.rag.dto.RagPrompt;

public interface RagAnswerGenerator {
	AiGenerateResult generate(RagPrompt prompt);
}
