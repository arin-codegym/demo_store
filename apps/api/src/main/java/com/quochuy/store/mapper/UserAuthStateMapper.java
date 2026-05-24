package com.quochuy.store.mapper;

import com.quochuy.redis.dto.AuthState;
import org.apache.ibatis.annotations.Mapper;

import java.util.UUID;

@Mapper
public interface UserAuthStateMapper {
	AuthState loadAuthState(UUID userId);
}
