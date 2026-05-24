package com.quochuy.store.controller;

import com.quochuy.security.CustomUserDetails;
import com.quochuy.chat.conversation.dto.ConversationSummaryResponse;
import com.quochuy.chat.message.service.ChatUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/backend/user")
@RequiredArgsConstructor
public class UserController {
	private final ChatUserService chatUserService;

	
	
	@GetMapping("/chatable")
	@PreAuthorize("isAuthenticated()")
	public List<ConversationSummaryResponse> getChatableUsers(
			@AuthenticationPrincipal CustomUserDetails currentUser,
			@RequestParam(required = false) String keyword
	) {
		if (currentUser == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
		}
		
		return chatUserService.getChatableUsers(currentUser.getUserId(), keyword);
	}

}
