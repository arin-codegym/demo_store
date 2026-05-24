package com.quochuy.security.services;

import com.quochuy.store.mapper.UserMapper;
import com.quochuy.store.mapper.UserRolesMapper;
import com.quochuy.store.model.User;
import com.quochuy.redis.model.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthUserService {
	private final UserMapper userMapper;
	private final UserRolesMapper userRoles;
	
	@Transactional
	public User findOrCreateGoogleUser(String googleSub, String email, String name, String avatarUrl) {
		// 1) match theo provider + sub
		User u = userMapper.findByProvider("GOOGLE", googleSub);
		if (u != null) {
			// optional: update profile nếu muốn (có thể viết thêm updateProfile)
			return u;
		}
		// 2) fallback theo email
		User byEmail = userMapper.findByEmail(email);
		if (byEmail != null) {
			// Option A: link local -> google
			byEmail.setProviderUserId(googleSub);
			byEmail.setFullName(name);
			byEmail.setAvatarUrl(avatarUrl);
			userMapper.updateGoogleLink(byEmail);
			return byEmail;
			
			// Option B: không link, báo lỗi
			// throw new IllegalStateException("Email đã tồn tại, hãy login bằng username/password.");
		}
		
		// 3) create new
		User newUser = new User();
		UUID userId =  UUID.randomUUID();
		newUser.setUserId(userId);
		newUser.setEmail(email);
		newUser.setFullName(name);
		newUser.setAvatarUrl(avatarUrl);
		newUser.setAuthProvider("GOOGLE");
		newUser.setProviderUserId(googleSub);
		newUser.setStatus(UserStatus.ACTIVE);
		
		// password null (vì Google login)
		newUser.setPassword(null);
		
		// username unique
		newUser.setUserName(generateUniqueUsername(email));
		newUser.setRoles(List.of("ROLE_USER"));
		
		userMapper.insertUser(newUser);
		userRoles.createRoleIsUser(userId);
		return newUser;
	}
	
	private String generateUniqueUsername(String email) {
		String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9._-]", "").toLowerCase();
		if (base.isBlank()) base = "user";
		
		String candidate = base;
		int i = 0;
		while (userMapper.existsByUsername(candidate)) {
			i++;
			candidate = base + "_" + i;
		}
		return candidate;
	}
}
