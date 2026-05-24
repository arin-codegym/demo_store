package com.quochuy.store.service.impl;

import com.quochuy.store.dto.request.CreateUserRequestBody;
import com.quochuy.store.dto.request.UpdateUserRequestBody;
import com.quochuy.store.mapper.AuthSessionMapper;
import com.quochuy.store.mapper.OrderMapper;
import com.quochuy.store.mapper.UserMapper;
import com.quochuy.store.mapper.UserRolesMapper;
import com.quochuy.store.model.Order;
import com.quochuy.store.model.User;
import com.quochuy.redis.dto.AuthState;
import com.quochuy.redis.model.UserStatus;
import com.quochuy.redis.service.AuthStateCache;
import com.quochuy.store.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
	private final ModelMapper modelMapper;
	private final OrderMapper orderMapper;
	private final UserMapper userMapper;
	private final UserRolesMapper userRolesMapper;
	private final AuthStateCache cache;
	private final AuthSessionMapper authSessionMapper;
	@Override
	public List<Order> getDashboardOrder() {
		return orderMapper.getDashboardOrder();
	}
	
	@Override
	public List<User> getDashboardUsers() {
		return userMapper.getDashboardUsers();
	}
	
	@Override
	@Transactional
	public void updateUser(UUID userId, UpdateUserRequestBody updateUserRequestBody) {
		if (updateUserRequestBody.getStatus()
				.equals(UserStatus.BANNED) || updateUserRequestBody.getStatus()
				.equals(UserStatus.DELETED)) {
			AuthState authState = userMapper.banUserReturning(userId,
															  updateUserRequestBody.getStatus());
			cache.setWithTtl(userId, authState, Duration.ofMinutes(1));
			authSessionMapper.revoke(userId, OffsetDateTime.now(ZoneOffset.UTC), "BANNED");
		} else {
			userMapper.updateActiveUser(userId, updateUserRequestBody.getStatus());
		}
		userRolesMapper.deleteUserRoles(userId);
		userRolesMapper.insertUserRoles(userId, updateUserRequestBody.getRoles());
	}
	
	@Override
	@Transactional
	public void createUser(CreateUserRequestBody createUserRequestBody) {
		User user = modelMapper.map(createUserRequestBody,User.class);
		UUID userId = UUID.randomUUID();
		BCryptPasswordEncoder brtyp = new BCryptPasswordEncoder();
		user.setUserId(userId);
		user.setAuthProvider("LOCAL");
		user.setPassword(brtyp.encode(createUserRequestBody.getPassword()));
		userMapper.createUser(user);
		userRolesMapper.insertUserRoles(userId,user.getRoles());
	}
//	@Transactional
//	public void banUser(UUID userId) {
//		int newVer = userRepo.bumpTokenVersionAndSetBanned(userId); // trả về ver mới
//		cache.set(userId, new AuthState(UserStatus.BANNED, newVer));
//	}
}
