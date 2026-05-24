package com.quochuy.security.config;

import com.quochuy.security.CustomUserDetails;
import com.quochuy.security.filters.AuthStateFilter;
import com.quochuy.security.services.CustomOidcUserService;
import com.quochuy.security.services.OAuthUserService;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import reactor.util.annotation.NonNull;

import javax.crypto.spec.SecretKeySpec;
import java.util.Collection;
import java.util.UUID;

/**
 * Central Spring Security configuration.
 *
 * <p>This class wires the three authentication paths used by the app:</p>
 * <ul>
 *   <li>Username/password login through custom controller endpoints.</li>
 *   <li>Google OAuth2/OIDC login through Spring Security OAuth2 Client.</li>
 *   <li>JWT validation for API requests through Spring Security Resource Server.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Log4j2
public class SecurityConfig {
	@Value("${app.jwt.secret}")
	private String jwtSecret;

	@Value("${app.frontend-url:http://localhost:3000}")
	private String frontendUrl;
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Converts the OIDC user returned by Google into the application's principal.
	 */
	@Bean
	OAuth2UserService<OidcUserRequest, OidcUser> customOidcUserService(
			OAuthUserService oAuthUserService) {
		return new CustomOidcUserService(oAuthUserService);
	}
	
	/**
	 * Exposes Spring's AuthenticationManager for username/password authentication flows.
	 */
	@Bean
	public AuthenticationManager authenticationManager(
			AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
	
	/**
	 * Builds the HTTP security filter chain.
	 *
	 * <p>JWT requests flow through:</p>
	 * <pre>
	 * request
	 *   -> BearerTokenAuthenticationFilter
	 *   -> bearerTokenResolver()
	 *   -> jwtDecoder()
	 *   -> jwtAuthenticationConverter()
	 *   -> SecurityContext
	 *   -> controller method / @AuthenticationPrincipal
	 * </pre>
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http
	, OAuth2UserService<OidcUserRequest, OidcUser> customOidcUserService
	, AuthenticationSuccessHandler oAuth2LoginSuccessHandler
	, AuthStateFilter authStateFilter
	) throws Exception {
		http.csrf(csrf -> csrf.disable())
				// Enables the CorsConfigurationSource bean before requests reach MVC controllers.
				.cors(cors ->{} )
				// OAuth2 login needs a short-lived server session for the authorization state.
				.sessionManagement(
						session -> session.sessionCreationPolicy(
								SessionCreationPolicy.IF_REQUIRED))
				.authorizeHttpRequests(
						auth -> auth.requestMatchers("/api/backend/auth/**")
								.permitAll()
								.requestMatchers("/api/backend/admin/**")
								.hasRole("ADMIN")
								.requestMatchers("/api/backend/cart/**").authenticated()
								.requestMatchers("/ws/**").authenticated()
								.requestMatchers("/api/backend/product/**").permitAll()
								.requestMatchers("/api/backend/review/**").permitAll()
								.requestMatchers("/api/backend/favorites/**").permitAll()
								// Keep broad API rules after the stricter matchers above. Spring uses the first match.
								.requestMatchers("/api/backend/**").permitAll()
								.requestMatchers("/**").permitAll()
								.anyRequest().authenticated()
				)
				.oauth2Login(
						o -> o
							// Nginx routes /api/backend/** to the backend, so OAuth endpoints use the same prefix.
							.authorizationEndpoint(a -> a.baseUri("/api/backend/oauth2/authorization"))
							.redirectionEndpoint(r -> r.baseUri("/api/backend/login/oauth2/code/*"))
							.userInfoEndpoint(u -> u.oidcUserService(customOidcUserService))
							.successHandler(oAuth2LoginSuccessHandler)
							.failureHandler((req, res, ex) -> {
								log.warn("OAuth2 login failed", ex);
								res.sendRedirect(frontendUrl + "/login?error=" + ex.getClass().getSimpleName());
							})
				)
				// Validates JWT bearer tokens for API requests.
				.oauth2ResourceServer(
						oauth2 -> oauth2.bearerTokenResolver(bearerTokenResolver())
						.jwt(jwt -> jwt.decoder(jwtDecoder())
						.jwtAuthenticationConverter(
								jwtAuthenticationConverter())
						
						)
						
				)
				.addFilterAfter(authStateFilter, BearerTokenAuthenticationFilter.class);
		return http.build();
	}
	
	/**
	 * Resolves access tokens from either the Authorization header or the accessToken cookie.
	 *
	 * <p>The default Spring resolver only supports {@code Authorization: Bearer ...}.
	 * This app also supports browser requests authenticated by an HTTP-only cookie.</p>
	 */
	@Bean
	public BearerTokenResolver bearerTokenResolver() {
		return request -> {
			String authHeader = request.getHeader("Authorization");
			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				return authHeader.substring(7);
			}

			if (request.getCookies() != null) {
				for (Cookie cookie : request.getCookies()) {
					if ("accessToken".equals(cookie.getName())) {
						return cookie.getValue();
					}
				}
			}
			return null;
		};
	}
	
	/**
	 * Decodes and verifies locally signed HS256 JWTs.
	 *
	 * <p>If a client changes a token payload, the HMAC signature no longer matches.
	 * Without {@code jwtSecret}, the client cannot generate a valid new signature.</p>
	 */
	@Bean
	public JwtDecoder jwtDecoder() {
		SecretKeySpec spec = new SecretKeySpec(jwtSecret.getBytes(),
											   "HmacSHA256");
		return NimbusJwtDecoder.withSecretKey(spec)
				.macAlgorithm(MacAlgorithm.HS256).build();
	}
	
	/**
	 * Converts a verified JWT into the CustomUserDetails principal used by controllers.
	 *
	 * <p>Spring's default JWT authentication exposes a raw Jwt principal. Returning a
	 * UsernamePasswordAuthenticationToken here lets controller methods receive
	 * CustomUserDetails through {@code @AuthenticationPrincipal}.</p>
	 */
	@Bean
	public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
		return new Converter<Jwt, AbstractAuthenticationToken>() {
			@Override
			public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
				JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
				authoritiesConverter.setAuthoritiesClaimName("roles");
				authoritiesConverter.setAuthorityPrefix("");

				Collection<GrantedAuthority> authorities = authoritiesConverter.convert(
						jwt);

				String userId = jwt.getSubject();
				String userName = jwt.getClaimAsString("userName");
				String email = jwt.getClaimAsString("email");

				Object v = jwt.getClaims().get("ver");
				int tokenVersion;
				try{
					tokenVersion = (v instanceof Number n) ? n.intValue() : -1;
				}catch (Exception ex){
					tokenVersion = -1;
					log.error(ex.getCause());
				}
				
				CustomUserDetails userDetails = new CustomUserDetails(UUID.fromString(userId),
																	  userName,
																	  email, "",
																	  authorities, tokenVersion, null);

				return new UsernamePasswordAuthenticationToken(userDetails,
													   null,
													   authorities);
			}
		};
	}
}
