package com.quochuy.store.mapper;

import com.quochuy.redis.dto.AuthState;
import com.quochuy.redis.model.UserStatus;
import org.apache.ibatis.annotations.Mapper;
import com.quochuy.store.model.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface UserMapper {
	// Bạn có thể dùng Annotation hoặc file XML (tùy sở thích viết SQL native)
//    @Select("SELECT * FROM users WHERE username = #{username}")
	User findByUsername(String username);

	
	void updateToken(String userName, String token);
	
	
	User findByProvider(@Param("provider") String provider,
						@Param("providerUserId") String providerUserId);
	
	User findByEmail(@Param("email") String email);
	boolean existsByUsername(@Param("username") String username);
	boolean existsByEmail(@Param("email") String email);
	
	void insertUser(User user);
	
	void updateGoogleLink(User user); // link existing user to google + update profile
	
	User findById(@Param("userId") UUID userId);
	
	List<User> getDashboardUsers();
	
	int updateActiveUser(UUID userId, UserStatus status);
	
	AuthState banUserReturning(UUID userId,UserStatus status);
	
	int createUser(User user);
	int createRegisteredUser(User user);
	int markEmailVerified(@Param("userId") UUID userId);
	
	UUID getAdminId();
}
