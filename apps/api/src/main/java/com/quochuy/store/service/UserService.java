package com.quochuy.store.service;

import com.quochuy.store.model.User;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface UserService {
	User findByUsername(String username);
	
	List<User> findAll();
	
	void insert(User user);
	
	void updateToken(String userName, String token);
	
	Authentication buildAuthentication(String username);
	
	Authentication buildAuthenticationByUserId(UUID userId);
}
