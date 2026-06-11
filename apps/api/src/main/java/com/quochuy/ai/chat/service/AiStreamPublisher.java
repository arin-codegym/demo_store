package com.quochuy.ai.chat.service;

import com.quochuy.ai.chat.enums.AiAnswerSource;
import com.quochuy.chat.realtime.ChatRealtimePublisher;
import com.quochuy.websocket.dto.WsEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AiStreamPublisher {
	private final ChatRealtimePublisher chatRealtimePublisher;
	
	public void publishDelta(
			UUID conversationId,
			String streamId,
			String delta,
			String content,
			AiAnswerSource source
	) {
		chatRealtimePublisher.publishToConversation(conversationId, WsEnvelope.builder()
				.type("ai.reply.delta")
				.data(Map.of(
						"conversationId", conversationId,
						"streamId", streamId,
						"delta", safe(delta),
						"content", safe(content),
						"source", source == null ? "" : source.name(),
						"createdAt", OffsetDateTime.now().toString()
				))
				.build());
	}
	
	public void publishDone(
			UUID conversationId,
			String streamId,
			UUID messageId,
			String content,
			AiAnswerSource source
	) {
		chatRealtimePublisher.publishToConversation(conversationId, WsEnvelope.builder()
				.type("ai.reply.done")
				.data(Map.of(
						"conversationId", conversationId,
						"streamId", streamId,
						"messageId", messageId,
						"content", safe(content),
						"source", source == null ? "" : source.name(),
						"createdAt", OffsetDateTime.now().toString()
				))
				.build());
	}
	
	private String safe(String value) {
		return value == null ? "" : value;
	}
}
