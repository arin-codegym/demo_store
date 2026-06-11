package com.quochuy.ai.chat.service;

import com.quochuy.ai.shared.dto.AiGenerateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class StreamingLlmAnswerGenerator {
	private static final Duration STREAM_TIMEOUT = Duration.ofSeconds(90);
	
	private final ChatClient.Builder chatClientBuilder;
	
	public AiGenerateResult generate(
			String systemPrompt,
			String userPrompt,
			String model,
			Consumer<String> onDelta
	) {
		long start = System.currentTimeMillis();
		StringBuilder content = new StringBuilder();
		
		chatClientBuilder.build()
				.prompt()
				.system(safe(systemPrompt))
				.user(safe(userPrompt))
				.stream()
				.content()
				.timeout(STREAM_TIMEOUT)
				.filter(delta -> delta != null && !delta.isEmpty())
				.doOnNext(delta -> {
					content.append(delta);
					if (onDelta != null) {
						onDelta.accept(delta);
					}
				})
				.blockLast();
		
		long latency = System.currentTimeMillis() - start;
		return AiGenerateResult.builder()
				.content(content.toString())
				.provider("spring-ai")
				.model(model)
				.promptTokens(null)
				.completionTokens(null)
				.totalTokens(null)
				.latencyMs(latency)
				.build();
	}
	
	private String safe(String value) {
		return value == null ? "" : value;
	}
}
