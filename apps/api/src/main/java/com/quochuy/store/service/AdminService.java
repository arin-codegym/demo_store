package com.quochuy.store.service;

import com.quochuy.store.dto.request.CreateUserRequestBody;
import com.quochuy.store.dto.request.UpdateUserRequestBody;
import com.quochuy.store.model.Order;

import java.util.List;
import java.util.UUID;

public interface AdminService {
	List<Order>getDashboardOrder();
	
	Object getDashboardUsers();
	
	void updateUser(UUID userId, UpdateUserRequestBody updateUserRequestBody);
	
	void createUser(CreateUserRequestBody createUserRequestBody);
}
