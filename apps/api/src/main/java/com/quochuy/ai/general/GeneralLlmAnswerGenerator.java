package com.quochuy.ai.general;

import com.quochuy.ai.shared.dto.AiGenerateResult;
import com.quochuy.ai.general.dto.GeneralPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

@RequiredArgsConstructor
public class GeneralLlmAnswerGenerator implements GeneralAnswerGenerator {
	
	private final ChatModel chatModel;
	
	@Override
	public AiGenerateResult generate(GeneralPrompt prompt) {
		long start = System.currentTimeMillis();
		
		Prompt springPrompt = new Prompt(List.of(
				new SystemMessage(prompt.systemPrompt()),
				new UserMessage(prompt.userPrompt())
		));
		
		ChatResponse response = chatModel.call(springPrompt);
		String content = response.getResult().getOutput().getText();
		
		long latency = System.currentTimeMillis() - start;
		
		return AiGenerateResult.builder()
				.content(content)
				.provider("spring-ai")
				.model("gemini-2.5-flash")
				.promptTokens(null)
				.completionTokens(null)
				.totalTokens(null)
				.latencyMs(latency)
				.build();
	}
}
