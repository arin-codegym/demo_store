package com.quochuy.store.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class EmailActivationToken {
	private UUID tokenId;
	private UUID userId;
	private String tokenHash;
	private OffsetDateTime expiresAt;
	private OffsetDateTime usedAt;
	private OffsetDateTime createdAt;
}
