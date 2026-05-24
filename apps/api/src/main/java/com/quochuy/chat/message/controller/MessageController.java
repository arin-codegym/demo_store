package com.quochuy.chat.message.controller;

import com.quochuy.security.CustomUserDetails;
import com.quochuy.chat.message.dto.MarkSeenRequest;
import com.quochuy.chat.message.dto.MessageResponse;
import com.quochuy.chat.message.dto.SendMessageRequest;
import com.quochuy.chat.message.service.MessageService;
import com.quochuy.chat.message.service.SeenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Follow
 * A (frontend)
 *   -> POST /api/conversations/10/messages
 *       -> MessageController
 *           -> MessageService
 *               -> save DB
 *               -> publishEvent(MessageCreatedEvent)
 *                   -> MessageCreatedRealtimeListener nghe event
 *                       -> StompChatRealtimePublisher
 *                           -> SimpMessagingTemplate.convertAndSend(...)
 *                               -> Spring STOMP broker
 *                                   -> WebSocket connection
 *                                       -> client A / client B nhận realtime
 * */
@RestController
@RequestMapping("/api/backend")
@RequiredArgsConstructor
public class MessageController {
	
	private final MessageService messageService;
	private final SeenService seenService;
	
	@PostMapping("/messages")
	@PreAuthorize("isAuthenticated()")
	public MessageResponse sendMessage(
			@AuthenticationPrincipal CustomUserDetails customUserDetails,
//			@PathVariable UUID conversationId,
			@Valid @RequestBody SendMessageRequest request
	) {
		return messageService.sendMessage(
				customUserDetails.getUserId(),
				request.getConversationId(),
				request.getClientMessageId(),
				request.getContent()
		);
	}
	
	@GetMapping("/conversations/{conversationId}/messages")
	@PreAuthorize("isAuthenticated()")
	public List<MessageResponse> getMessages(
			@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@PathVariable UUID conversationId,
			@RequestParam(required = false) UUID beforeMessageId,
			@RequestParam(defaultValue = "30") Integer limit
	) {
		return messageService.getMessages(customUserDetails.getUserId(), conversationId, beforeMessageId,
										  limit);
	}
	
	@PostMapping("/conversations/{conversationId}/seen")
	@PreAuthorize("isAuthenticated()")
	public void markSeen(
			@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@PathVariable UUID conversationId,
			@Valid @RequestBody MarkSeenRequest request
	) {
		seenService.markSeen(customUserDetails.getUserId(), conversationId, request.getLastReadMessageId());
	}
}
