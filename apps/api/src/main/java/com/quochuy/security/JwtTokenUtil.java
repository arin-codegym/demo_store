package com.quochuy.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.stream.Collectors;

@Component
public class JwtTokenUtil {
	private final JwtEncoder encoder;
	private final JwtDecoder decoder;
	@Value("${app.jwt.secret}")
	private String jwtSecret;
	@Value("${app.jwt.issuer}")
	private String jwtIssuer;
	
	public JwtTokenUtil(@Value("${app.jwt.secret}") String jwtSecret) {
		this.jwtSecret = jwtSecret;
		this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecret.getBytes()));
		// Khởi tạo decoder để dùng cho việc validate và extract thủ công (ví dụ trong Refresh Token)
		SecretKeySpec spec = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
		this.decoder = NimbusJwtDecoder.withSecretKey(spec).macAlgorithm(MacAlgorithm.HS256)
				.build();
	}
	
	/**
	 * Phương thức bổ trợ để tạo Token với các tham số tùy chỉnh.
	 */
	public String createToken(String subject, String userName, String email, String scope,
							  long expirationSeconds) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(jwtIssuer)
				.issuedAt(now)
				.expiresAt(now.plusSeconds(expirationSeconds))
				.subject(subject)
				.claim("roles", scope)
				.claim("userName", userName)
				.claim("email", email)
				.build();
		/*Đã move lên constructor*/
		//		JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecret.getBytes()));
		//		return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256)
		//				.build(), claims)).getTokenValue();
		return this.encoder.encode(
						JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
				.getTokenValue();
	}
//	public String createAccessTokenWithSession(
//			String subject, String userName, String email, String scope,
//			long expirationSeconds, String sessionId
//	) {
//		Instant now = Instant.now();
//		JwtClaimsSet claims = JwtClaimsSet.builder()
//				.issuer(jwtIssuer)
//				.issuedAt(now)
//				.expiresAt(now.plusSeconds(expirationSeconds))
//				.subject(subject)
//				.claim("sid", sessionId)       // ✅ session id
//				.claim("roles", scope)
//				.claim("userName", userName)
//				.claim("email", email)
//				.build();
//
//		return this.encoder.encode(
//				JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)
//		).getTokenValue();
//	}
	
	//	public String generateAccessToken(Authentication authentication) {
//		// Ép kiểu về CustomUserDetails để lấy userId thực tế
//		CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
//		// Lấy quyền (Role) của User
//		// CHỐT CHẶN BẢO MẬT:
//		// Chỉ lấy các quyền bắt đầu bằng "ROLE_" (ví dụ: ROLE_ADMIN,
//		// ROLE_USER)
//		// Việc này giúp loại bỏ các "FACTOR_" hoặc các quyền mặc định khác từ
//		// Supabase/OAuth2
//		String scope = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
//				.filter(authority -> authority.startsWith("ROLE_"))
//				.collect(Collectors.joining(" "));
//		// Tạo tập hợp các Claims (thông tin chứa trong Token)
//		return createToken(user.getUserId(), user.getUsername(), user.getEmail(), scope, 3600);
//	}
	public String generateAccessToken(Authentication authentication, String sessionId) {
		CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
		String scope = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.filter(authority -> authority.startsWith("ROLE_"))
				.collect(Collectors.joining(" "));
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(jwtIssuer)
				.issuedAt(now)
				.expiresAt(now.plusSeconds(3600)) // 15 phút (bạn có thể đổi)
				.subject(user.getUserId().toString())
				.claim("sid", sessionId)
				.claim("roles", scope)
				.claim("userName", user.getUsername())
				.claim("email", user.getEmail() != null?user.getEmail():"")
				.claim("ver", user.getTokenVersion()) // ✅ thêm dòng này
				.build();
		return this.encoder.encode(
						JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
				.getTokenValue();
	}
	
	/**
	 * Tạo Refresh Token với thời hạn dài (thường là 7 ngày). Refresh Token thường không cần chứa
	 * "roles" để giảm kích thước và tăng bảo mật, vì nó chỉ dùng để cấp lại Access Token mới.
	 */
	public String generateRefreshToken(Authentication authentication) {
		// Refresh token thường để trống scope hoặc chỉ chứa thông tin định danh
		CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
		// Thời hạn 7 ngày (7 * 24 * 60 * 60 = 604800 giây)
		// Chúng ta có thể để trống "scope" cho Refresh Tokenauthentication
		return createToken(user.getUserId().toString(), user.getUsername(), user.getEmail(), "",
						   604800);
	}
//	public boolean validateAccessToken(String token) {
//		try {
//			/* Bản chất decoder.decode(token) :
//				✔️ Check:
//				chữ ký
//				hết hạn
//				cấu trúc JWT
//				❌ KHÔNG check:
//				issuer
//				audience
//				scope*/
//			Jwt jwt = decoder.decode(token);
//			// (OPTIONAL) check issuer
//			if (!jwtIssuer.equals(jwt.getIssuer().toString())) {
//				return false;
//			}
//			// 2. (OPTIONAL) Check expiration (decoder đã check) tùy biến thêm hoăc không thêm
//			// nếu cần
//			// 3. (OPTIONAL) Check scope / roles
//			return true;
//		} catch (JwtException e) {
//			return false;
//		}
//	}
//	public boolean validateRefreshToken(String token) {
//		//		try {
//		//			JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(jwtSecret.getBytes(), "HMACSHA256"))
//		//					.build();
//		//			decoder.decode(token); // Nếu lỗi (hết hạn, sai chữ ký) nó sẽ ném
//		//			// ra Exception
//		//			return true;
//		//		} catch (Exception e) {
//		//			return false;
//		//		}
//		try {
//			decoder.decode(token);
//			return true;
//		} catch (JwtException e) {
//			return false;
//		}
//	}
	/**
	 * Trích xuất Subject (UserId) từ Token
	 */
//	public String extractSubject(String token) {
//		return decoder.decode(token).getSubject();
//	}
//
//	public String extractUsernameFromAccessToken(String token) {
//		return decoder.decode(token).getClaimAsString("userName");
//	}
//	public String getUsernameFromRefreshToken(String token) {
//		//		// 1. Khởi tạo Decoder với Secret Key của bạn
//		//		JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(new SecretKeySpec(jwtSecret.getBytes(), "HMACSHA256"))
//		//				.build();
//		//		// 2. Giải mã token
//		//		Jwt jwt = decoder.decode(token);
//		//		// 3. Trích xuất username từ claim "subject"
//		//		return jwt.getSubject();
//		return decoder.decode(token).getClaimAsString("userName");
//	}
}
