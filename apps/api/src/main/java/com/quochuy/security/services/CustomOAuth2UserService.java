//package com.quochuy.store.security.services;
//
//import com.quochuy.store.model.User;
//import com.quochuy.store.security.CustomUserDetails;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//
//public class CustomOAuth2UserService
//		implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
//
//	private final OAuthUserService oAuthUserService;
//
//	public CustomOAuth2UserService(OAuthUserService oAuthUserService) {
//		this.oAuthUserService = oAuthUserService;
//	}
//
//	@Override
//	public OAuth2User loadUser(OAuth2UserRequest userRequest)
//			throws OAuth2AuthenticationException {
//
//		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate =
//				new DefaultOAuth2UserService();
//
//		OAuth2User oauthUser = delegate.loadUser(userRequest);
//
//		String email = oauthUser.getAttribute("email");
//		String name = oauthUser.getAttribute("name");
//		String picture = oauthUser.getAttribute("picture");
//		String sub = oauthUser.getAttribute("sub");
//
//		User user = oAuthUserService.findOrCreateGoogleUser(sub, email, name, picture);
//
//		CustomUserDetails base = CustomUserDetails.build(user);
//
//		return new AppOAuth2Principal(base, oauthUser);
//	}
//}
