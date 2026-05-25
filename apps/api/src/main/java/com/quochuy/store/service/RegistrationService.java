package com.quochuy.store.service;

import com.quochuy.common.exception.AppException;
import com.quochuy.common.exception.enums.UserErrorCode;
import com.quochuy.security.session.RefreshTokenHasher;
import com.quochuy.security.session.TokenFactory;
import com.quochuy.store.dto.request.RegisterRequest;
import com.quochuy.store.mapper.EmailActivationTokenMapper;
import com.quochuy.store.mapper.UserMapper;
import com.quochuy.store.mapper.UserRolesMapper;
import com.quochuy.store.model.EmailActivationToken;
import com.quochuy.store.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {
	private static final int ACTIVATION_HOURS = 24;

	private final UserMapper userMapper;
	private final UserRolesMapper userRolesMapper;
	private final EmailActivationTokenMapper tokenMapper;
	private final PasswordEncoder passwordEncoder;
	private final TokenFactory tokenFactory;
	private final RefreshTokenHasher tokenHasher;
	private final EmailService emailService;

	@Value("${app.frontend-url:http://localhost:3000}")
	private String frontendUrl;

	@Transactional
	public void register(RegisterRequest request) {
		if (userMapper.existsByUsername(request.getUserName())) {
			throw new AppException(UserErrorCode.USERNAME_ALREADY_EXISTS);
		}
		if (userMapper.existsByEmail(request.getEmail())) {
			throw new AppException(UserErrorCode.EMAIL_ALREADY_EXISTS);
		}

		UUID userId = UUID.randomUUID();
		User user = new User();
		user.setUserId(userId);
		user.setUserName(request.getUserName());
		user.setFullName(request.getFullName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRoles(List.of("ROLE_USER"));

		userMapper.createRegisteredUser(user);
		userRolesMapper.createRoleIsUser(userId);

		String activationToken = tokenFactory.newRefreshTokenRaw();
		EmailActivationToken token = new EmailActivationToken();
		token.setTokenId(UUID.randomUUID());
		token.setUserId(userId);
		token.setTokenHash(tokenHasher.hash(activationToken));
		token.setExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusHours(ACTIVATION_HOURS));
		tokenMapper.insert(token);

		String activationUrl = frontendUrl + "/activate?token=" + activationToken;
		emailService.sendActivationEmail(request.getEmail(), request.getFullName(), activationUrl);
	}

	@Transactional
	public void activate(String rawToken) {
		if (rawToken == null || rawToken.isBlank()) {
			throw new AppException(UserErrorCode.ACTIVATION_TOKEN_INVALID);
		}

		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		EmailActivationToken token = tokenMapper.findValidByHash(tokenHasher.hash(rawToken), now);
		if (token == null) {
			throw new AppException(UserErrorCode.ACTIVATION_TOKEN_INVALID);
		}

		userMapper.markEmailVerified(token.getUserId());
		tokenMapper.markUsed(token.getTokenId(), now);
		tokenMapper.revokeUnusedByUserId(token.getUserId(), now);
	}
}
