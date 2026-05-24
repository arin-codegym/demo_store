package com.quochuy.security;

import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Map;

public class AppOidcPrincipal extends CustomUserDetails implements OidcUser {
	private final OidcUser delegate;
//	private final Collection<? extends GrantedAuthority> authorities;//muốn authorities khác với
//	base
//	.getAuthorities().
	public AppOidcPrincipal(
			CustomUserDetails base,
			OidcUser delegate
//			Collection<? extends GrantedAuthority> authorities
	) {
		super(base.getUserId(),
			  base.getUsername(),
			  base.getEmail(),
			  base.getPassword(),
			  base.getAuthorities(),
			  base.getTokenVersion(),
			  base.getStatus()
		);
		this.delegate = delegate;
//		this.authorities = authorities;
	}
	@Override
	public Map<String, Object> getClaims() {
		return delegate.getClaims();
	}
	
	@Override
	public OidcUserInfo getUserInfo() {
		return delegate.getUserInfo();
	}
	
	@Override
	public OidcIdToken getIdToken() {
		return delegate.getIdToken();
	}
	
	@Override
	public Map<String, Object> getAttributes() {
		return delegate.getAttributes();
	}
	
	@Override
	public String getName() {
		return delegate.getName();
	}
	// authorities: ưu tiên authorities app của bạn (ROLE_*)
//	@Override
//	public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
}
