package com.quochuy.store.controller;

import com.quochuy.security.session.AuthSessionService;
import com.quochuy.security.session.RefreshTokenHasher;
import com.quochuy.security.session.TokenFactory;
import com.quochuy.store.dto.request.LoginRequest;
import com.quochuy.common.exception.enums.AuthErrorCode;
import com.quochuy.common.exception.AppException;
import com.quochuy.store.mapper.AuthSessionMapper;
import com.quochuy.store.model.AuthSession;
import com.quochuy.store.model.User;
import com.quochuy.redis.dto.AuthState;
import com.quochuy.redis.model.UserStatus;
import com.quochuy.redis.service.AuthStateCache;
import com.quochuy.redis.service.UserAuthStateService;
import com.quochuy.security.CustomUserDetails;
import com.quochuy.security.JwtTokenUtil;
import com.quochuy.store.service.impl.UserServiceImpl;
import com.quochuy.utils.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/backend/auth")
@RequiredArgsConstructor
@Log4j2
public class AuthController {
	private final AuthenticationManager authenticationManager;
	private final JwtTokenUtil jwtTokenUtil;
	private final UserAuthStateService userAuthStateService;
	private final UserServiceImpl userServiceImpl;
	private final ModelMapper modelMapper;
	private final AuthSessionService authSessionService;
	private final AuthSessionMapper authSessionMapper;
	private final RefreshTokenHasher refreshTokenHasher;
	private final TokenFactory tokenFactory;
	private final AuthStateCache cache;
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest,
								   HttpServletRequest request, HttpServletResponse response) {
		try {
			/**             1. Xác thực bằng Username/Password
			 Spring Security sẽ gọi loadUserByUsername
			 Hàm này trả về đối tượng CustomUserDetails
			 với data từ db. authenticate() có phương
			 thức compare password nên bắt buộc
			 CustomUserDetails phải tạo field password
			 mặc dù không xài nhưng phaỉ có nếu không
			 sẽ không thể nào xác thực được
			 Lúc này, Spring Security cần cái mật khẩu đã mã hóa (hashed password) từ DB (nằm trong CustomUserDetails)
			 để so sánh với mật khẩu người dùng vừa nhập vào.
			 loadUserByUsername -> so password -> trả về Authentication đã authenticated*/
			Authentication authRequest = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
															loginRequest.getPassword()));
			CustomUserDetails userDetails = (CustomUserDetails) authRequest.getPrincipal();
			if (userDetails.getStatus() == UserStatus.BANNED) {
				throw new AppException(AuthErrorCode.USER_BANNED);
			}
			if(userDetails.getStatus() == UserStatus.DELETED){
				throw new AppException(AuthErrorCode.USER_DELETED);
			}
			/**authRequest.getPrincipal()  → CustomUserDetails
			 authRequest.getAuthorities() → ROLE_USER, ROLE_ADMIN
			 authRequest.isAuthenticated() → true*/
			/**Nhét Authentication vào SecurityContext của request hiện tại
			 Đánh dấu: “request này đã được xác thực thành công” nghĩa là lúc này chưa có token
			 nhưng nói spring rằng đã xác thực rồi cứ yên tâm
			 AuthenticationManager.authenticate() KHÔNG tự set
			 Spring Security KHÔNG đoán
			 Nếu không set() -> authentication == null*/
			SecurityContextHolder.getContext().setAuthentication(authRequest);
			/**
			 SecurityContextHolder.getContext().setAuthentication(authentication); chỉ chạy duy
			 nhất 1 lần khi người dùng gửi Username/Password.
			 Bối cảnh: Lúc này chưa có Token.
			 Hành động: Bạn vừa xác thực xong với DB, bạn có đối tượng authentication trong tay, bạn chủ động nhét nó vào túi để "đánh dấu" Thread này đã an toàn.
			 Kết thúc: Sau khi API Login trả về kết quả (kèm Token), cái túi này của Thread đó sẽ bị xóa sạch.
			 */
			CustomUserDetails principal = (CustomUserDetails) authRequest.getPrincipal();
			
			// 🔐 1️⃣ Tạo session + refresh token
			String ip = request.getRemoteAddr();
			String ua = request.getHeader("User-Agent");
			var createdSession = authSessionService.createNewSession(principal.getUserIdAsUuid(),
																	 ip, ua);
			// 🔐 2️⃣ Tạo access JWT có sid
			String accessToken = jwtTokenUtil.generateAccessToken(authRequest,
																  createdSession.sessionId()
																		  .toString());
			ResponseCookie accessCookie = CookieUtils.createResponseCookie("accessToken",
																		   accessToken,
																		   Duration.ofMinutes(60),
																		   false);
			// 3. Tạo Refresh Token (chỉ cần username)
			String refreshToken = createdSession.refreshTokenRaw();
			ResponseCookie refreshCookie = CookieUtils.createResponseCookie("refreshToken",
																			refreshToken,
																			Duration.ofDays(7),
																			false);
			ResponseCookie sidCookie = CookieUtils.createResponseCookie("sid",
																		createdSession.sessionId().toString(),
																		Duration.ofDays(7), false);
			// Cache redis
			
			AuthState state = userAuthStateService.loadAuthState(principal.getUserId());
			cache.set(principal.getUserId(),state);
			
			return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
					.header(HttpHeaders.SET_COOKIE, accessCookie.toString())
					.header(HttpHeaders.SET_COOKIE, sidCookie.toString())
					.body(Map.of("accessToken : ",accessCookie) );
		} catch (BadCredentialsException e) {
			throw new AppException(AuthErrorCode.INVALID_CREDENTIALS);
		}
	}
	
	/**
	 * API Lấy thông tin người dùng hiện tại (Profile) Spring Security sẽ tự động giải mã JWT và
	 * tiêm đối tượng Authentication vào hàm này. vì sao không setAuthentication() . Follow
	 * Authorization: Bearer token → JwtAuthenticationFilter → validate token → build Authentication
	 * → set SecurityContext → Controller (/me)
	 */
	@GetMapping("/me")
	public ResponseEntity<?> getCurrentUser(Authentication authentication) {
		/*Đã move sang JwtAuthenticationFilter interceptor*/
		// Lấy token thô từ Header gửi lên
		//		String currentToken = request.getHeader("Authorization").substring(7); // chỉ áp
		//		dụng khi gửi kèm headers trong request ví dụ key= Authorization value = `Bearer
		//		token...`
		//		if (user.getJwtToken() == null || !user.getJwtToken().equals(currentToken)) {
		//			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
		//					.body(Map.of("error", "Token không hợp lệ hoặc đã đăng nhập ở nơi khác"));
		//		}
		//		// 3. Xây dựng một Response đầy đủ thông tin cho Frontend (Next.js)
		//		Map<String, Object> profile = Map.of("id", user.getUserId(), "username", user.getUserName(), "email", user.getEmail() != null ? user.getEmail() : "", "fullName", user.getFullName() != null ? user.getFullName() : "", "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "", "roles", user.getRoles()
		//				//				authentication.getAuthorities().stream()
		//				//						.map(GrantedAuthority::getAuthority)
		//				//						.collect(Collectors.toList())
		//		);
		// Nếu không có Token hoặc Token sai, Spring Security đã chặn từ tầng Filter (401)
		// Nếu vào được đến đây, authentication chắc chắn không null
		// 1. Lấy username từ đối tượng Authentication (đã được Filter giải mã từ JWT)
		if (authentication == null || !authentication.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		String username = authentication.getName();// lấy trực tiếp từ http-only
		// 2. Truy vấn Database để lấy thông tin mới nhất và đầy đủ nhất
		User user = userServiceImpl.findByUsername(username);
		Map<String, Object> profile = Map.of("userId", user.getUserId(), "username", user.getUserName(),
											 "email",
											 user.getEmail() != null ? user.getEmail() : "",
											 "fullName",
											 user.getFullName() != null ? user.getFullName() : "",
											 "avatarUrl",
											 user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
											 "roles", user.getRoles());
		return ResponseEntity.ok(profile);
	}
	
	/** TẠI SAO /refresh-token KHÔNG SET Authentication
	 * Vì refresh-token:
	 * Không cần role
	 * Không cần @PreAuthorize
	 * Không truy cập resource bảo vệ
	 * Chỉ:
	 * 	validate refresh token
	 * 	tạo access token mới
	 */
	@GetMapping("/refresh")
	public ResponseEntity<?> refreshToken(HttpServletRequest request,
										  @CookieValue(name = "refreshToken", required = false) String refreshTokenRaw,
										  @CookieValue(name = "sid", required = false) String sid) {
		if (refreshTokenRaw == null || sid == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		UUID sessionId;
		try {
			sessionId = UUID.fromString(sid);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		// 1) Load session từ DB (hoặc Redis cache nếu bạn có)
		AuthSession session = authSessionMapper.findActiveBySessionId(sessionId);
		if (session == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		// 2) Verify refresh token: hash(cookie) phải match DB
		String presentedHash = refreshTokenHasher.hash(refreshTokenRaw);
		if (!refreshTokenHasher.constantTimeEquals(presentedHash, session.getRefreshTokenHash())) {
			// OPTIONAL (mạnh hơn): revoke session ngay khi token sai (phòng token theft)
			authSessionMapper.revoke(sessionId, OffsetDateTime.now(ZoneOffset.UTC),
									 "REFRESH_TOKEN_MISMATCH");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		// 3) Build Authentication từ user_id trong session (không cần username từ token)
		// Bạn có thể load userDetails theo userId hoặc query ra username rồi loadUserByUsername
		// Ví dụ: userService.buildAuthenticationByUserId(session.getUserId())
		Authentication authentication = userServiceImpl.buildAuthenticationByUserId(
				session.getUserId());
		Object p = authentication.getPrincipal();
		log.info("principal class = {}", p.getClass().getName());
		log.info("principal = {}", p);
		if (authentication == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		// 4) Rotate refresh token (cực khuyến nghị)
		String newRefreshRaw = tokenFactory.newRefreshTokenRaw();
		String newRefreshHash = refreshTokenHasher.hash(newRefreshRaw);
		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
		OffsetDateTime newExpiresAt = now.plusDays(7);
		String ip = request.getRemoteAddr();
		String ua = request.getHeader("User-Agent");
		int updated = authSessionMapper.rotateRefreshToken(sessionId, newRefreshHash, now, ip, ua,
														   newExpiresAt);
		if (updated != 1) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		// 5) Issue access token mới (JWT) có sid
		String newAccessToken = jwtTokenUtil.generateAccessToken(authentication,
																 sessionId.toString());
		// 6) Set cookies
		ResponseCookie accessCookie = CookieUtils.createResponseCookie("accessToken",
																	   newAccessToken,
																	   Duration.ofMinutes(60),
																	   false);
		ResponseCookie refreshCookie = CookieUtils.createResponseCookie("refreshToken",
																		newRefreshRaw,
																		Duration.ofDays(7), false);
		// sid cookie giữ nguyên (hoặc refresh TTL lại cho đồng bộ)
		ResponseCookie sidCookie = CookieUtils.createResponseCookie("sid", sessionId.toString(),
																	Duration.ofDays(7), false);
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
		headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
		headers.add(HttpHeaders.SET_COOKIE, sidCookie.toString());
//		return ResponseEntity.ok().header(
//				HttpHeaders.SET_COOKIE, accessCookie.toString())
//				.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
//				.header(HttpHeaders.SET_COOKIE, sidCookie.toString()).build();
		return ResponseEntity.ok().headers(headers).build();
	}
}

