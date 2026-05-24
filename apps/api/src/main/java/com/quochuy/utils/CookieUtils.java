package com.quochuy.utils;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtils {
	
	/**
	 * Tạo ResponseCookie với các cấu hình bảo mật tiêu chuẩn.
	 * * @param name     Tên cookie (ví dụ: "sid")
	 * @param value    Giá trị (ví dụ: session id)
	 * @param duration Thời gian tồn tại (Dùng Duration.ofDays(7))
	 * @param isSecure Có bắt buộc dùng HTTPS không (Nên là true ở Production)
	 * @return ResponseCookie
	 */
	public static ResponseCookie createResponseCookie(String name, String value, Duration duration, boolean isSecure) {
		return ResponseCookie.from(name, value)
				.httpOnly(true)                // Chống XSS
				.secure(isSecure)             // true nếu chạy HTTPS
				.path("/")                    // Có hiệu lực cho toàn bộ domain
				.maxAge(duration)             // Tự động chuyển sang giây
				.sameSite("Lax")              // Chống CSRF cơ bản
				.build();
	}
	
	// Overload method cho trường hợp dùng mặc định 7 ngày (như yêu cầu của bạn)
	public static ResponseCookie createSessionCookie(String value, boolean isSecure) {
		return createResponseCookie("sid", value, Duration.ofDays(7), isSecure);
	}
}