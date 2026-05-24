package com.quochuy.store.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthSession {
	private UUID sessionId;
	private UUID userId;
	private String refreshTokenHash;
	private String ipFirst; // inet -> map String ok
	private String ipLast;
	private String uaFirst;
	private String uaLast;
	private OffsetDateTime createdAt;
	private OffsetDateTime lastSeenAt;
	private OffsetDateTime expiresAt;
	private String status;      // ACTIVE / REVOKED
	private OffsetDateTime revokedAt;
	private String revokeReason;
}
