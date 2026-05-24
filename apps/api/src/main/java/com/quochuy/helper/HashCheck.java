package com.quochuy.helper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class HashCheck {
	static String fp(byte[] b) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] dig = md.digest(b);
			return Base64.getUrlEncoder().withoutPadding().encodeToString(dig).substring(0, 12);
		} catch (Exception e) { throw new RuntimeException(e); }
	}
	
	static String fp(String s) {
		return fp(s.getBytes(StandardCharsets.UTF_8));
	}
}
