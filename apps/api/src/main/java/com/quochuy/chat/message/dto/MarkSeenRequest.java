package com.quochuy.chat.message.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MarkSeenRequest {
	@NotNull
	private UUID lastReadMessageId;
}
