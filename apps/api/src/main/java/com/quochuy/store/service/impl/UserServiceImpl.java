package com.quochuy.store.service.impl;

import com.quochuy.store.mapper.UserMapper;
import com.quochuy.store.model.User;
import com.quochuy.redis.model.UserStatus;
import com.quochuy.security.CustomUserDetails;
import com.quochuy.store.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Log4j2
public class UserServiceImpl implements UserService {
	private final UserMapper userMapper;
	final UserDetailsService userDetailsService;
	
	public UserServiceImpl(UserMapper userMapper, UserDetailsService userDetailsService) {
		this.userMapper = userMapper;
		this.userDetailsService = userDetailsService;
	}
	
	@Override
	public Authentication buildAuthentication(String username) {
		CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);
		Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		return auth;
	}
	
	@Override
	public Authentication buildAuthenticationByUserId(UUID userId) {
		User user = userMapper.findById(userId);
		if (user == null ) {
			return null; // hoặc throw Unauthorized
		}
		// ✅ check theo status mới
		if (user.getStatus() != UserStatus.ACTIVE) {   // hoặc "ACTIVE".equals(user.getStatusStr())
			return null;
		}
		
		List<String> roles = user.getRoles();
		
		List<GrantedAuthority> authorities = roles.stream()
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());
		
		CustomUserDetails principal = CustomUserDetails.builder()
				.userId(user.getUserId())   // hoặc UUID tùy class bạn
				.userName(user.getUserName())
				.email(user.getEmail())
				.password(user.getPassword()) // có thể null nếu không cần
				.authorities(authorities)
				.tokenVersion(user.getTokenVersion()) // ✅ rất quan trọng
				.build();
		log.debug("db version = {}", user.getTokenVersion());
		log.debug("principal version right after build = {}", principal.getTokenVersion());
		log.debug("principal authorities right after build = {}", principal.getAuthorities());
		
		// credentials = null vì không cần password ở refresh
		return new UsernamePasswordAuthenticationToken(principal, null, authorities);
	}
	
	@Override
	public User findByUsername(String username) {
		return userMapper.findByUsername(username);
	}
	
	@Override
	public List<User> findAll() {
		return null;
	}
	
	@Override
	public void insert(User user) {
	}
	
	@Override
	public void updateToken(String userName, String token) {
		userMapper.updateToken(userName, token);
	}
}
