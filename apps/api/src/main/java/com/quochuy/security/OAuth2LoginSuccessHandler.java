package com.quochuy.security;

import com.quochuy.security.session.AuthSessionService;
import com.quochuy.security.services.OAuthUserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Log4j2
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
	private final JwtTokenUtil jwtTokenUtil;
	private final OAuthUserService oAuthUserService;
	private final AuthSessionService authSessionService;

	@Value("${app.frontend-url:http://localhost:3000}")
	private String frontendUrl;
	
	/*Code base đơn giản*/
//	@Override
//	public void onAuthenticationSuccess(HttpServletRequest request,
//										HttpServletResponse response,
//										Authentication authentication) throws IOException {
//
//		OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
//		OAuth2User oAuth2User = oauthToken.getPrincipal();
//
//		String email = oAuth2User.getAttribute("email");
//		String name  = oAuth2User.getAttribute("name");
//		String picture = oAuth2User.getAttribute("picture");
//		String sub = oAuth2User.getAttribute("sub");
//		if (sub == null) sub = email;
//
//		User user = oAuthUserService.findOrCreateGoogleUser(sub, email, name, picture);
//
//		String accessToken = jwtTokenUtil.createToken(
//				user.getUserId().toString(),
//				user.getUserName(),
//				user.getEmail(),
//				"ROLE_USER",
//				3600
//		);
//		String refreshToken = jwtTokenUtil.createToken(
//				user.getUserId().toString(),
//				user.getUserName(),
//				user.getEmail(),
//				"",
//				604800
//		);
//
//		response.setContentType("application/json");
//		response.getWriter().write("""
//      { "token": "%s" }
//    """.formatted(accessToken));
//	}
	/*Note: có nhiều config nhưng nhớ
 Form login / Username login - UsernamePasswordAuthenticationToken
 OAuth2 Login -OAuth2AuthenticationToken
 JWT Resource Server -JwtAuthenticationToken
 Tất cả khác class nhưng tất cả đều Authentication tức là impelement
 	authentication.getPrincipal()
	authentication.getAuthorities()
	authentication.isAuthenticated()
	Nhưng:
	principal có thể là CustomUserDetails
	hoặc OidcUser
	hoặc Jwt
	hoặc AppOidcPrincipal
	hoặc cái gì bạn tự define
		Case A — User login Google
		SecurityContext
			Authentication = OAuth2AuthenticationToken
				principal = AppOidcPrincipal
		Case B — User login username/password
		SecurityContext
			Authentication = UsernamePasswordAuthenticationToken
				principal = CustomUserDetails
		Case C — User call API bằng JWT
		SecurityContext
			Authentication = JwtAuthenticationToken
				principal = Jwt (hoặc Custom nếu bạn convert)
 Search các key trên trong source sẽ hiểu các trường hợp sử dụng
* */
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
										Authentication authentication) throws IOException {
		log.debug("AUTH CLASS: " + authentication.getClass());
		log.debug("PRINCIPAL CLASS: " + authentication.getPrincipal().getClass());
		log.debug("AUTHORITIES: " + authentication.getAuthorities());
		CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
		
		String ip = request.getRemoteAddr();
		String ua = request.getHeader("User-Agent");
		
		// 1) tạo session + refresh token raw
		var created = authSessionService.createNewSession(user.getUserIdAsUuid(), ip, ua);
		// 2) tạo access JWT có sid
		String accessToken = jwtTokenUtil.generateAccessToken(authentication,
															  created.sessionId().toString());
		
		// 3) set cookie httpOnly
		String refreshToken = jwtTokenUtil.generateRefreshToken(authentication); // ✅ dùng lại được
        boolean secureCookie = frontendUrl.startsWith("https://");
        // access cookie
		ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
				.httpOnly(true)
				.secure(secureCookie)            // ✅ local http -> false, prod https -> true
				.sameSite("Lax")          // ✅ thường ok cho OAuth redirect dạng top-level
				.path("/")
				.maxAge(Duration.ofMinutes(15))
				.build();
		
		// refresh cookie
		ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
				.httpOnly(true)
				.secure(secureCookie)            // prod -> true
				.sameSite("Lax")
				.path("/")                // hoặc "/auth" nếu bạn muốn scope hẹp
				.maxAge(Duration.ofDays(7))
				.build();
		response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
		response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
		// IMPORTANT: đã redirect thì không write JSON nữa
		response.sendRedirect(frontendUrl + "/");
	}
	private void addHttpOnlyCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
		Cookie cookie = new Cookie(name, value);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);         // prod nên true (HTTPS)
		cookie.setPath("/");
		cookie.setMaxAge(maxAgeSeconds);
		// SameSite không có setter trong javax Cookie (tuỳ version), thường set bằng header:
		response.addHeader("Set-Cookie",
						   String.format("%s=%s; Max-Age=%d; Path=/; HttpOnly; Secure; SameSite=Lax",
										 name, value, maxAgeSeconds));
	}
}

