package com.quochuy.ai.business;

import com.quochuy.ai.shared.dto.AiGenerateResult;
import com.quochuy.ai.business.dto.AiBusinessPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

@RequiredArgsConstructor
public class BusinessLlmAnswerGenerator {
	
	private final ChatModel chatModel;
	
	public AiGenerateResult generate(AiBusinessPrompt prompt) {
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
				.model("business-chat-model")
				.promptTokens(null)
				.completionTokens(null)
				.totalTokens(null)
				.latencyMs(latency)
				.build();
	}
}
