package com.quochuy.store.dto.request;

import com.quochuy.redis.model.UserStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateUserRequestBody {
	@NotBlank
	private String userName;
	@NotBlank
	private String fullName;
	//	@Email(message = "Email không hợp lệ")
	// "" pass
	@Pattern(regexp = "^$|^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Email không hợp lệ")
	private String email;
	@NotBlank
	@Size(min = 6)
	private String password;
	@NotNull
	private UserStatus status;
	@NotEmpty
	private List<String> roles;
}
