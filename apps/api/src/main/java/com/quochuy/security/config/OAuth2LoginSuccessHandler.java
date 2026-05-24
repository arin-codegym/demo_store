package com.quochuy.security.config;

import com.quochuy.security.CustomUserDetails;
import com.quochuy.security.session.AuthSessionService;
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

/**
 * Completes a successful Google/OIDC login.
 *
 * <p>Spring Security has already authenticated the Google user before this
 * handler runs. The principal is our {@link CustomUserDetails} wrapper, created
 * by {@code CustomOidcUserService}. This handler then issues the same browser
 * session cookies used by username/password login:</p>
 *
 * <ul>
 *   <li>{@code accessToken}: short-lived JWT used by API requests.</li>
 *   <li>{@code refreshToken}: opaque token stored hashed in {@code auth_sessions}.</li>
 *   <li>{@code sid}: session id used to find and rotate the refresh token.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
	private final JwtTokenUtil jwtTokenUtil;
	private final AuthSessionService authSessionService;

	@Value("${app.frontend-url:http://localhost:3000}")
	private String frontendUrl;

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication
	) throws IOException {
		log.debug("AUTH CLASS: {}", authentication.getClass());
		log.debug("PRINCIPAL CLASS: {}", authentication.getPrincipal().getClass());
		log.debug("AUTHORITIES: {}", authentication.getAuthorities());

		CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
		String ip = request.getRemoteAddr();
		String userAgent = request.getHeader("User-Agent");

		var created = authSessionService.createNewSession(user.getUserIdAsUuid(), ip, userAgent);
		String accessToken = jwtTokenUtil.generateAccessToken(
				authentication,
				created.sessionId().toString()
		);

		boolean secureCookie = frontendUrl.startsWith("https://");
		ResponseCookie accessCookie = createCookie(
				"accessToken",
				accessToken,
				Duration.ofMinutes(15),
				secureCookie
		);
		ResponseCookie refreshCookie = createCookie(
				"refreshToken",
				created.refreshTokenRaw(),
				Duration.ofDays(7),
				secureCookie
		);
		ResponseCookie sidCookie = createCookie(
				"sid",
				created.sessionId().toString(),
				Duration.ofDays(7),
				secureCookie
		);

		response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
		response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
		response.addHeader(HttpHeaders.SET_COOKIE, sidCookie.toString());

		// OAuth is a browser redirect flow, so the response must redirect instead of writing JSON.
		response.sendRedirect(frontendUrl + "/");
	}

	private ResponseCookie createCookie(
			String name,
			String value,
			Duration maxAge,
			boolean secure
	) {
		return ResponseCookie.from(name, value)
				.httpOnly(true)
				.secure(secure)
				.sameSite("Lax")
				.path("/")
				.maxAge(maxAge)
				.build();
	}
}
