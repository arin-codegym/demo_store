package com.quochuy.security.session;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RefreshHashConfig {
	
	@Bean
	public RefreshTokenHasher refreshTokenHasher(
			@Value("${spring.security.refresh-hash-secret}")
			String secret
	) {
		return new RefreshTokenHasher(secret);
	}
	@Bean
	public ApplicationRunner runner(RefreshTokenHasher hasher) {
		return args -> {
			System.out.println("refresh secret fp = " + hasher.secretFingerprint());
		};
	}
}
