package com.quochuy.security;

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

import javax.crypto.spec.SecretKeySpec;
import java.util.Collection;
import java.util.UUID;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Log4j2
public class SecurityConfig {
//	private final JwtTokenUtil jwtTokenUtil;
//	private final OAuthUserService oAuthUserService; // ✅ inject service
//	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler; // đang tiêm trong method
	@Value("${app.jwt.secret}")
	private String jwtSecret;

	@Value("${app.frontend-url:http://localhost:3000}")
	private String frontendUrl;
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	@Bean
	OAuth2UserService<OidcUserRequest, OidcUser> customOidcUserService(
			OAuthUserService oAuthUserService) {
		return new CustomOidcUserService(oAuthUserService);
	}
	
	@Bean
	public AuthenticationManager authenticationManager(
			AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
	
	/*HTTP request
	   ↓
	BearerTokenAuthenticationFilter
	   ↓
	1. bearerTokenResolver()
	   ↓
	2. jwtDecoder()
	   ↓
	3. jwtAuthenticationConverter()
	   ↓
	Authentication object
	   ↓
	SecurityContext
	   ↓
	@Controller
	@AuthenticationPrincipal hoạt động
	*/
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http
	, OAuth2UserService<OidcUserRequest, OidcUser> customOidcUserService
	, AuthenticationSuccessHandler oAuth2LoginSuccessHandler
	, AuthStateFilter authStateFilter
	) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.cors(cors ->{} )// <-- chỗ này khiến Spring Security dùng CorsConfigurationSource bean
				
				/*Dòng này nói với Spring Security rằng:
				❌ Không tạo HTTP Session
				❌ Không lưu SecurityContext vào session
				❌ Mỗi request phải tự mang thông tin xác thực (JWT)
				Follow:
				Client → gửi JWT trong Authorization header
				Server → verify JWT
				Server → xử lý request
				Server → quên hết (không lưu session)*/
//				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				/*lưu session khi cần -> nói thẳng ra là thằng google nó cần session*/
				.sessionManagement(
						session -> session.sessionCreationPolicy(
								SessionCreationPolicy.IF_REQUIRED))
				.authorizeHttpRequests(
						auth -> auth.requestMatchers("/api/backend/auth/**")
								.permitAll() // Endpoint đăng nhập
								.requestMatchers("/api/backend/admin/**")
								.hasRole("ADMIN")
								// Thêm dòng này để xác định rõ các API giỏ hàng cần đăng nhập
								// Đây được xem như cánh cổng 1 thêm @PreAuthorize ở controller
								// tức là thêm cánh cổng thứ 2
								// Quy tắc 1
								.requestMatchers("/api/backend/cart/**").authenticated()
								.requestMatchers("/ws/**").authenticated()
								.requestMatchers("/api/backend/product/**").permitAll()
								.requestMatchers("/api/backend/review/**").permitAll()
								.requestMatchers("/api/backend/favorites/**").permitAll()
								// đặt sau cùng
								// nếu đặt nó ở đầu tiên thì các pmatcher sau
								// nó không ăn vì có /api khí đó phải chặn
								// cổng 2 @PreAuthorize
								.requestMatchers("/api/backend/**").permitAll()// Quy tắc 2
								.requestMatchers("/**").permitAll()// Quy tắc 2
								.anyRequest().authenticated()
				)
				//  Google Login
//				=> Nghĩa là oAuth2SuccessHandler() là method trong SecurityConfig
//				trả về AuthenticationSuccessHandler.
//				.oauth2Login(
//						oauth2 -> oauth2.successHandler(oAuth2SuccessHandler()))
				.oauth2Login(
						/** Dùng mặc định của oauth2*/
//						oauth2 -> oauth2.successHandler(
//						oAuth2LoginSuccessHandler)/*Code base đơn giản*/
						/*Tạo custom hứng principal của google map thành principal của app tự
						define*/
						o -> o
							.userInfoEndpoint(u ->
													  /** Dùng mặc định của oauth2*/
//													  u.userService(customOAuth2UserService)
													  u.oidcUserService(customOidcUserService)
							) //
									// inject bean custom
									.successHandler(oAuth2LoginSuccessHandler)
									.failureHandler((req, res, ex) -> {
										ex.printStackTrace();
										res.sendRedirect(frontendUrl + "/login?error=" + ex.getClass().getSimpleName());
									}) // thêm cái này để thấy lỗi rõ
				)
				//  JWT verify cho API
				// Cấu hình Resource Server để tự giải mã JWT bằng Secret Key
				.oauth2ResourceServer(
				/* oauth2ResourceServer(oauth2 -> oauth2.jwt()) // use mặc định
					Spring Security tự động add filter sau:BearerTokenAuthenticationFilter
					tương đương việc tự tạo JwtAuthenticationFilter nếu không dùng
					<artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
				*/
						oauth2 -> oauth2.bearerTokenResolver(bearerTokenResolver())
						.jwt(jwt -> jwt.decoder(jwtDecoder())
						.jwtAuthenticationConverter(
										jwtAuthenticationConverter())
						
						)
						
				)
				.addFilterAfter(authStateFilter, BearerTokenAuthenticationFilter.class);
		return http.build();
	}
	
	/*Custom nhiều kiểu lấy token
	* mặc định của BearerTokenResolver chỉ parse kiểu Authorization:`Bearer
	* token` */
	@Bean
	public BearerTokenResolver bearerTokenResolver() {
		return request -> {
			// 1 ưu tiên Authorization header
			String authHeader = request.getHeader("Authorization");
			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				return authHeader.substring(7);
			}
			// 2 fallback sang cookie
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
	
	/*	 * Giả sử hacker muốn đổi role từ USER thành ADMIN:
	 * <p>
	 * Hacker sửa Payload (Base64 rất dễ).
	 * <p>
	 * Khi Payload thay đổi, chữ ký cũ trên Token trở nên vô giá trị vì nó không còn khớp với nội
	 * dung mới.
	 * <p>
	 * Hacker muốn tạo chữ ký mới cho nội dung ADMIN? Không thể, vì hacker không có Secret Key để
	 * chạy hàm $HMAC\_SHA256$ ra kết quả giống Server.*/
	@Bean
	public JwtDecoder jwtDecoder() {
		/* Sử dụng Secret Key để giải mã thay vì gọi URL issuer (tránh lỗi localhost khi deploy)*/
		SecretKeySpec spec = new SecretKeySpec(jwtSecret.getBytes(),
											   "HmacSHA256");
		return NimbusJwtDecoder.withSecretKey(spec)
				.macAlgorithm(MacAlgorithm.HS256).build();
	}
	/**
	 Default của Security nên Chỉ cho phép bạn cấu hình cách lấy Role.
	 Đối tượng Principal mà bạn nhận được ở Controller sẽ là một đối tượng Jwt thô (chỉ là các chuỗi).
	 Không thể gọi .getUserId() từ một chuỗi được. JwtAuthenticationConverter là implements onverter<Jwt, AbstractAuthenticationToken>
	 * */
	//		@Bean
	//	public JwtAuthenticationConverter jwtAuthenticationConverter() {
	//		// Converter để Spring hiểu các "Role" nằm trong trường "role" của JWT
	//		JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
	//		// 1. Chỉ định tên Claim: Phải khớp 100% với claim("role", ...) trong JwtTokenUtil
	//		grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
	//		// 2. Tiền tố (Prefix):
	//		// - Nếu trong DB/Token của bạn đã có chữ "ROLE_USER" thì đặt là "" (chuỗi rỗng).
	//		// - Nếu trong DB/Token chỉ có chữ "USER" thì đặt là "ROLE_".
	//		// Vì CustomUserDetailsService của bạn đang dùng SimpleGrantedAuthority("ROLE_USER")
	//		// nên chúng ta để trống tiền tố ở đây.
	//		grantedAuthoritiesConverter.setAuthorityPrefix("");
	//		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
	//		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
	//		return jwtAuthenticationConverter;
	//	}
	
	/**
	 * jwtAuthenticationConverter chạy mỗi khí có request và tất nhiên phải pass
	 * được bước Verification Verification tức là giả mã một chiều đi tạo chữ ký
	 * từ secret key rồi compare xem có giống chữ ký token gửi lên không . bản
	 * chất token hoàn toàn có thể giải mã ngược ra thông tin (claim) và chữ ký.
	 * Tùy biến - Deep Dive): Chúng ta không dùng class có sẵn mà tự viết lại
	 * cách "Dịch" (Convert). Đoạn code này cho phép chúng ta can thiệp vào hàm
	 * getPrincipal() để nhét ông CustomUserDetails vào. Tại sao không thể giả
	 * mạo?
	 * <p>
	 * Việc tạo method này còn giúp bỏ hẳn JwtAuthenticationFilter Follow
	 * Request ↓ BearerTokenAuthenticationFilter (Spring) ↓ JwtDecoder (HS256) ↓
	 * jwtAuthenticationConverter (CUSTOM) ↓ JwtAuthenticationToken └──
	 * principal = CustomUserDetails ↓ SecurityContextHolder ↓
	 *
	 * @Controller
	 * @AuthenticationPrincipal CustomUserDetails
	 */
	@Bean
	public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
		return new Converter<Jwt, AbstractAuthenticationToken>() {
			@Override
			public AbstractAuthenticationToken convert(Jwt jwt) {
				/* 1. Lấy Roles từ Claim "roles"*/
				JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
				authoritiesConverter.setAuthoritiesClaimName("roles");
				authoritiesConverter.setAuthorityPrefix("");
				/*giải mã ngược jwt để lấy các thông tin từ token*/
				Collection<GrantedAuthority> authorities = authoritiesConverter.convert(
						jwt);
				/* 2. Trích xuất thông tin để tạo CustomUserDetails
				 Lưu ý: "sub" thường là userId hoặc username tùy cách bạn tạo JWT*/
				String userId = jwt.getSubject();
				String userName = jwt.getClaimAsString("userName");
				String email = jwt.getClaimAsString(
						"email"); // Giả sử bạn có claim "email"
//				String tokenVersion = jwt.getClaimAsString("ver");
				Object v = jwt.getClaims().get("ver");
				int tokenVersion;
				try{
					tokenVersion = (v instanceof Number n) ? n.intValue() : -1;
				}catch (Exception ex){
					tokenVersion = -1;
					log.error(ex.getCause());
				}
				
				/* Tạo đối tượng Principal là CustomUserDetails*/
				CustomUserDetails userDetails = new CustomUserDetails(UUID.fromString(userId),
																	  userName,
																	  email, "",
																	  // Password không cần
																	  authorities,tokenVersion,null);
				/** 3. Trả về Token chứa CustomUserDetails làm Principal
				 Đây là bước quan trọng để @AuthenticationPrincipal nhận đúng loại dữ liệu
				 thường thì mặc định của nó là trả về JwtAuthenticationToken để filter khác tiếp
				 tục logic xử khác ví dụ AuthStateFilter nó sẽ làm việc như
				 if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
				 chain.doFilter(request, response);
				 return;
				 } nhưng ta trả về UsernamePasswordAuthenticationToken => tức là trả về thằng
				 custom là rồi CustomUserDetails
				 */
				//				return new JwtAuthenticationToken(jwt, authorities, userDetails.getUsername()) {
				//					@Override
				//					public Object getPrincipal() {
				//						return userDetails;
				//					}
				//				};
				// 🔥 KHÔNG dùng JwtAuthenticationToken nữa
				return new UsernamePasswordAuthenticationToken(userDetails,
															   null,
															   authorities);
			}
		};
	}
//	@Bean
//	public AuthenticationSuccessHandler oAuth2SuccessHandler() {
//		return (request, response, authentication) -> {
//			OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
//			OAuth2User oAuth2User = oauthToken.getPrincipal();
//			String email = oAuth2User.getAttribute("email");
//			String name = oAuth2User.getAttribute("name");
//			String picture = oAuth2User.getAttribute("picture");
//			String sub = oAuth2User.getAttribute("sub");
//			if (sub == null) sub = email;
//			// ✅ gọi MyBatis service
//			User user = oAuthUserService.findOrCreateGoogleUser(sub, email,
//																name, picture);
//			String roles = "ROLE_USER";
//			String token = jwtTokenUtil.createToken(user.getUserId().toString(),
//													user.getUserName(),
//													user.getEmail(), roles,
//													3600);
//			response.setContentType("application/json");
//			response.getWriter().write("""
//											     { "token": "%s" }
//											   """.formatted(token));
//		};
//	}
}
