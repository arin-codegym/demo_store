package com.quochuy.security.config;

import com.quochuy.security.session.TokenFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityFilterBeansConfig {
	@Value("${app.frontend-url:http://localhost:3000}")
	private String frontendUrl;

	@Bean
	public TokenFactory tokenFactory() {
		return new TokenFactory();
	}

	/**
	 * CORS for requests that pass through Spring Security.
	 *
	 * <p>Security filters run before the MVC layer, so defining CORS only with
	 * WebMvcConfigurer is not enough for authenticated endpoints or preflight
	 * OPTIONS requests. SecurityConfig enables this bean through http.cors().</p>
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of(frontendUrl, "http://localhost:3000"));
		config.setAllowCredentials(true);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of(
				"Content-Type",
				"Authorization",
				"X-Requested-With"
		));
		config.setExposedHeaders(List.of("Set-Cookie"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
