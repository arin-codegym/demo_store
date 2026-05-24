package com.quochuy.security.session;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
public final class TokenFactory {
	// 32 bytes = 256-bit entropy (đủ mạnh cho refresh token)
	private static final int REFRESH_TOKEN_BYTES = 32;
	
	private final SecureRandom secureRandom = new SecureRandom();
	
	/** session_id: UUID v4 */
	public UUID newSessionId() {
		return UUID.randomUUID();
	}
	
	/** refresh_token_raw: random bytes -> base64url (no padding) */
	public String newRefreshTokenRaw() {
		byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
