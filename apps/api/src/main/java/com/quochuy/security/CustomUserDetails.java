package com.quochuy.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.quochuy.store.model.User;
import com.quochuy.redis.model.UserStatus;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Class này đóng vai trò là "vỏ bọc" (wrapper) cho class User của bạn.
 * Nó giúp Spring Security hiểu được thông tin người dùng từ class User gốc
 * và cho phép bạn mang theo userId đi khắp nơi trong ứng dụng.
 */
@Builder
@Getter
public class CustomUserDetails implements UserDetails {
	private static final long serialVersionUID = 1L;
	@Getter
	private UUID userId; // Khớp với String userId của bạn
	private String userName;
	@Getter
	private String email;
	@JsonIgnore
	private String password;
	private Collection<? extends GrantedAuthority> authorities;
	private int tokenVersion;
	private UserStatus status;
	
	public CustomUserDetails(UUID userId
			, String userName
			, String email
			, String password
			, Collection<? extends GrantedAuthority> authorities
			, int tokenVersion
			,UserStatus status
	) {
		this.userName = userName;
		this.userId = userId;
		this.email = email;
		this.password = password;
		this.authorities = authorities;
		this.tokenVersion = tokenVersion;
		this.status = status;
	}
	
	/**
	 * Phương thức tĩnh để xây dựng CustomUserDetails từ đối tượng User Entity.
	 * Đây là cầu nối quan trọng giữa Database và Spring Security.
	 */
	public static CustomUserDetails build(User user) {
		// Chuyển đổi List<String> roles của bạn thành List<GrantedAuthority>
		List<GrantedAuthority> authorities = user.getRoles()
				.stream()
				.map(role -> new SimpleGrantedAuthority(role))
				.collect(Collectors.toList());
		return new CustomUserDetails(
				user.getUserId(),
				user.getUserName(),
				user.getEmail(),
				user.getPassword(),
				authorities ,
				user.getTokenVersion(),
				user.getStatus()
		);
	}
//	// 1. Private Constructor: Chỉ cho phép tạo object thông qua Builder
//	private CustomUserDetails(Builder builder) {
//		this.userId = builder.userId;
//		this.userName = builder.userName;
//		this.email = builder.email;
//		this.password = builder.password;
//		this.authorities = builder.authorities;     // ✅ thêm
//		this.tokenVersion = builder.tokenVersion;   // ✅ thêm
//	}
	
	public int getTokenVersion() {
		return tokenVersion;
	}
	
	// 2. Static Inner Class - Builder
//	public static class Builder {
//
//		private UUID userId; // Khớp với String userId của bạn
//		private String userName;
//		private String email;
//		@JsonIgnore
//		private String password;
//		private Collection<? extends GrantedAuthority> authorities;
//		int tokenVersion;
//		public UserStatus status;
//		public Builder userId(UUID userId) {
//			this.userId = userId;
//			return this; // Trả về chính nó để gọi nối tiếp (Fluent API)
//		}
//
//		public Builder username(String userName) {
//			this.userName = userName;
//			return this;
//		}
//		public Builder email(String email) {
//			this.email = email;
//			return this; // Trả về chính nó để gọi nối tiếp (Fluent API)
//		}
//		public Builder password(String password) {
//			this.password = password;
//			return this; // Trả về chính nó để gọi nối tiếp (Fluent API)
//		}
//		public Builder authorities(Collection<? extends GrantedAuthority> authorities) {
//			this.authorities = authorities;
//			return this; // Trả về chính nó để gọi nối tiếp (Fluent API)
//		}
//		public Builder tokenVersion(int tokenVersion) {
//			this.tokenVersion = tokenVersion;
//			return this; // Trả về chính nó để gọi nối tiếp (Fluent API)
//		}
//		public Builder status(UserStatus status) {
//			this.status = status;
//			return this; // Trả về chính nó để gọi nối tiếp (Fluent API)
//		}
//
//		// 3. Phương thức build() để tạo object thực sự
//		public CustomUserDetails build() {
//			return new CustomUserDetails(this);
//		}
//	}
//
//	// 4. Phương thức static để bắt đầu quá trình build
//	public static Builder builder() {
//		return new Builder();
//	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
	}
	
	@Override
	public String getPassword() {
		return this.password;
	}
	
	@Override
	public String getUsername() {
		return this.userName;
	}
	
	// Các thiết lập bảo mật mặc định (có thể tùy chỉnh nếu cần logic khóa tài khoản)
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}
	
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}
	
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CustomUserDetails user = (CustomUserDetails) o;
		return Objects.equals(userId, user.userId);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(userId);
	}
	
	public UUID getUserIdAsUuid() {
		return this.userId;
	}
}
