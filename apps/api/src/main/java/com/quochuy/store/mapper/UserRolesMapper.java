package com.quochuy.store.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface UserRolesMapper {
	void createRoleIsUser(@Param("userId")UUID userId);
	
	int deleteUserRoles(UUID userId);
	
	int insertUserRoles(UUID userId, List<String> roles);
	

}
