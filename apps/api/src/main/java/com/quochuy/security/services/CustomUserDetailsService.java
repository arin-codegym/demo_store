package com.quochuy.security.services;

import com.quochuy.security.CustomUserDetails;
import com.quochuy.store.mapper.UserMapper;
import com.quochuy.store.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
	/*	@Autowired
		private UserMapper userMapper; // MyBatis Mapper bạn đã viết
		Khai báo final để đảm bảo mapper không bị thay đổi sau khi khởi tạo*/
	private final UserMapper userMapper;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// ĐÂY CHÍNH LÀ CHỖ MYBATIS ĐƯỢC GỌI
		User user = userMapper.findByUsername(username);
		
		if (user == null) {
			throw new UsernameNotFoundException("Không tìm thấy người dùng: " + username);
		}
		log.info("dbHash={}", user.getPassword());
		// Chuyển List<String> roles từ DB thành List<SimpleGrantedAuthority> cho Spring
		List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
				.map(SimpleGrantedAuthority::new).collect(Collectors.toList());
		// Trả về User chuẩn của Spring Security
		// Lưu ý: Password trong database BẮT BUỘC phải được mã hóa bằng BCrypt
		//		return org.springframework.security.core.userdetails.User
		//				.withUsername(user.getUserName())
		//				.password(user.getPassword()) // Mật khẩu đã mã hóa trong DB
		////				.authorities(Collections.emptyList()) // Bạn có thể lấy Role từ DB nốt vào đây
		//				.authorities(authorities)
		//				.build();
		return CustomUserDetails.build(user);
	}
}
