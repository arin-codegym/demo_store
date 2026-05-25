package com.quochuy.store.mapper;

import com.quochuy.store.model.EmailActivationToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.OffsetDateTime;
import java.util.UUID;

@Mapper
public interface EmailActivationTokenMapper {
	int insert(EmailActivationToken token);

	EmailActivationToken findValidByHash(
			@Param("tokenHash") String tokenHash,
			@Param("now") OffsetDateTime now
	);

	int markUsed(
			@Param("tokenId") UUID tokenId,
			@Param("usedAt") OffsetDateTime usedAt
	);

	int revokeUnusedByUserId(
			@Param("userId") UUID userId,
			@Param("usedAt") OffsetDateTime usedAt
	);
}
