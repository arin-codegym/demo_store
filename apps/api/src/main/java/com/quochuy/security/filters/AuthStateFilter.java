package com.quochuy.security.filters;

import com.quochuy.redis.dto.AuthState;
import com.quochuy.redis.model.UserStatus;
import com.quochuy.redis.service.AuthStateCache;
import com.quochuy.redis.service.UserAuthStateService;
import com.quochuy.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class AuthStateFilter extends OncePerRequestFilter {
	private final AuthStateCache cache;
	private final UserAuthStateService userAuthStateService;
	
	public AuthStateFilter(AuthStateCache cache, UserAuthStateService userAuthStateService) {
		this.cache = cache;
		this.userAuthStateService = userAuthStateService;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails customUserDetails)) {
			chain.doFilter(request, response);// cho next đi tiếp
			return;
		}
	
		//	userId = UUID.fromString(jwt.getSubject()); nếu jwtAuthenticationConverter trả về dạng Jwt
		UUID userId = customUserDetails.getUserId();
		int jwtVer = customUserDetails.getTokenVersion();
		
		AuthState state = cache.get(userId);
		if (state == null) {
			// fallback DB
			state = userAuthStateService.loadAuthState(userId);
			if (state == null) {
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				return;
			}
			cache.set(userId, state);
		}

		if (state.status() == UserStatus.BANNED) {
			writeJson(response, 403, "ACCOUNT_BANNED", "Tài khoản đã bị khóa");
			return;
		}
		if (state.status() == UserStatus.DELETED) {
			writeJson(response, 401, "ACCOUNT_DELETED", "Tài khoản không tồn tại");
			return;
		}
		if (state.ver() != jwtVer) {
			writeJson(response, 401, "TOKEN_REVOKED", "Phiên đăng nhập đã hết hiệu lực");
			return;
		}
		
		chain.doFilter(request, response);// cho next đi tiếp ví dụ nếu còn filter còn không thì
		// tới controller
	}
	private void writeJson(HttpServletResponse response, int status, String code, String message) throws IOException {
		response.setStatus(status);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(
				"{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}"
		);
	}
}
