package com.quochuy.security.session;
import com.quochuy.store.mapper.AuthSessionMapper;
import com.quochuy.store.model.AuthSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class AuthSessionService {
	private static final long REFRESH_DAYS = 7;
	private final TokenFactory tokenFactory;
	private final RefreshTokenHasher refreshTokenHasher;
	private final AuthSessionMapper authSessionMapper;
	
	public CreatedSession createNewSession(UUID userId, String ip, String userAgent) {
		UUID sessionId = tokenFactory.newSessionId();
		String refreshTokenRaw = tokenFactory.newRefreshTokenRaw();
		String refreshTokenHash = refreshTokenHasher.hash(refreshTokenRaw);
		
		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		OffsetDateTime expiresAt = now.plusDays(REFRESH_DAYS);
		
		AuthSession session = AuthSession.builder()
				.sessionId(sessionId)
				.userId(userId)
				.refreshTokenHash(refreshTokenHash)
				.ipFirst(ip)
				.ipLast(ip)
				.uaFirst(userAgent)
				.uaLast(userAgent)
				.createdAt(now)
				.lastSeenAt(now)
				.expiresAt(expiresAt)
				.status("ACTIVE")
				.build();
		authSessionMapper.insert(session);
		return new CreatedSession(sessionId, refreshTokenRaw, expiresAt);
	}
	
	public record CreatedSession(UUID sessionId, String refreshTokenRaw, OffsetDateTime expiresAt) {}
	
}
