package com.quochuy.chat.conversation.controller;

import com.quochuy.security.CustomUserDetails;
import com.quochuy.chat.conversation.dto.ConversationSummaryResponse;
import com.quochuy.chat.conversation.dto.CreateDirectConversationRequest;
import com.quochuy.chat.message.service.ChatUserService;
import com.quochuy.chat.conversation.service.ConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/backend/conversations")
@RequiredArgsConstructor
public class ConversationController {
	private final ConversationService conversationService;
	private final ChatUserService chatUserService;
	
	@PostMapping("/direct")
	@PreAuthorize("isAuthenticated()")
	public Map<String, Object> createOrGetDirectConversation(
			@AuthenticationPrincipal CustomUserDetails customUserDetails,
			@Valid @RequestBody CreateDirectConversationRequest request) {
		UUID conversationId = conversationService.createOrGetDirectConversation(
				customUserDetails.getUserId(), request.getTargetUserId());
		return Map.of("conversationId", conversationId);
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping
	public List<ConversationSummaryResponse> getConversations(
			@AuthenticationPrincipal CustomUserDetails customUserDetails) {
		return conversationService.getConversationSummaries(customUserDetails.getUserId());
	}
	
	@GetMapping("/admin/me")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> ensureAdminConversation(
			@AuthenticationPrincipal CustomUserDetails currentUser
	) {
		return conversationService.getAdminConversation(currentUser.getUserId());
	}
	
	@GetMapping("/ai/me")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> ensureAiConversation(
			@AuthenticationPrincipal CustomUserDetails currentUser,
			@RequestParam(defaultValue = "general") String assistantCode
	) {
		return conversationService.getAiConversation(currentUser.getUserId(),assistantCode);
	}
}
