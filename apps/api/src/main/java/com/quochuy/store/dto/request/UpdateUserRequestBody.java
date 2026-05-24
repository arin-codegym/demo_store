package com.quochuy.store.dto.request;

import com.quochuy.redis.model.UserStatus;
import com.quochuy.validation.AllowedRoles;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRequestBody {
	@NotNull
	private UserStatus status;
	@AllowedRoles(
			value = { "ROLE_USER", "ROLE_ADMIN" },
			notNull = false,   // null = không update roles
			notEmpty = false,  // [] được phép tuỳ
			unique = true
	)
	private List<String> roles;
}
