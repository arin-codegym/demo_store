package com.quochuy.security.services;

import com.quochuy.store.model.User;
import com.quochuy.security.AppOidcPrincipal;
import com.quochuy.security.CustomUserDetails;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {
	
	private final OidcUserService delegate = new OidcUserService();
	private final OAuthUserService oAuthUserService;
	
	public CustomOidcUserService(OAuthUserService oAuthUserService) {
		this.oAuthUserService = oAuthUserService;
	}
	
	@Override
	public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
		OidcUser oidcUser = delegate.loadUser(userRequest);
		
		// OIDC standard claims (Google)
		String email = oidcUser.getEmail();
		String name = oidcUser.getFullName();
		String picture = oidcUser.getPicture();
		String sub = oidcUser.getIdToken().getSubject(); // claim "sub"
		// getSubject có trong IdToken claims accessor
		// :contentReference[oaicite:3]{index=3}
		
		User user = oAuthUserService.findOrCreateGoogleUser(sub, email, name, picture);
		
		CustomUserDetails base =  CustomUserDetails.build(user);
		
//		Set<GrantedAuthority> authorities = new HashSet<>();
//		authorities.addAll(base.getAuthorities());
//		// hoặc ép ROLE_USER:
//		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		
		return new AppOidcPrincipal(
				base
				, oidcUser
//				, authorities
		);
	}
}
