package com.quochuy.store.mapper;

import com.quochuy.store.model.AuthSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.OffsetDateTime;
import java.util.UUID;

@Mapper
public interface AuthSessionMapper {
	int insert(AuthSession session);
	
	AuthSession findActiveBySessionId(@Param("sessionId") UUID sessionId);
	
	int updateLastSeen(@Param("sessionId") UUID sessionId,
					   @Param("lastSeenAt") OffsetDateTime lastSeenAt,
					   @Param("ipLast") String ipLast, @Param("uaLast") String uaLast);
	
	int revoke(@Param("sessionId") UUID sessionId, @Param("revokedAt") OffsetDateTime revokedAt,
			   @Param("reason") String reason);
	
	int rotateRefreshToken(
			@Param("sessionId") UUID sessionId,
			@Param("newHash") String newHash,
			@Param("lastSeenAt") OffsetDateTime lastSeenAt,
			@Param("ipLast") String ipLast,
			@Param("uaLast") String uaLast,
			@Param("newExpiresAt") OffsetDateTime newExpiresAt
	);
}
