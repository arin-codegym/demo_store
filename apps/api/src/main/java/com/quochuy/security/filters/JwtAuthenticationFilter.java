//package com.quochuy.store.security;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
///*Không có JwtAuthenticationFilter thì
//@PreAuthorize, @AuthenticationPrincipal có vô tác dụng 100%
//Hệ quả nếu KHÔNG có JwtAuthenticationFilter
//EX: @GetMapping("/me")
//public User me(Authentication authentication) {
//    // authentication == null
//}
//@PreAuthorize("hasRole('ROLE_ADMIN')")
//→ luôn bị chặn
//@AuthenticationPrincipal CustomUserDetails user
//→ null
// Hiện tại không cần file này vì đang dùng .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
// .decoder(jwtDecoder()) tại config và nhớ không lại doFilterInternal tránh lỗi double
// authentication issue lớn chỉ dùng lại file
// này
// khi cần các trường hợp
// sau
//✔ Trường hợp hợp lệ
//
//		Check blacklist token
//
//		Multi-tenant (X-Tenant-ID)
//
//		Audit / logging bảo mật
//
//		Rotate key custom
//
//		Thêm attribute vào request
//ví dụ
//public class AuditFilter extends OncePerRequestFilter {
//	protected void doFilterInternal(...) {
//		log.info("Request from IP {}", request.getRemoteAddr());
//		filterChain.doFilter(request, response);
//	}
//}
//*/
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//	private final JwtTokenUtil jwtTokenUtil;
//	private final UserDetailsService userDetailsService;
//
//	public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, UserDetailsService userDetailsService) {
//		this.jwtTokenUtil = jwtTokenUtil;
//		this.userDetailsService = userDetailsService;
//	}
//
//	@Override
//	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//		String authHeader = request.getHeader("Authorization");
//		if (authHeader != null && authHeader.startsWith("Bearer ")) {
//			String accessToken = authHeader.substring(7);
//			if (jwtTokenUtil.validateAccessToken(accessToken)) {
//				String username = jwtTokenUtil.extractUsernameFromAccessToken(accessToken);
//				UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//				// 🔥 BUILD AUTHENTICATION THỦ CÔNG TẠI ĐÂY
//				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
//				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//				SecurityContextHolder.getContext().setAuthentication(authentication);
//			}
//		}
//		filterChain.doFilter(request, response);
//	}
//}

