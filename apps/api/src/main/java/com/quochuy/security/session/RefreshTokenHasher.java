package com.quochuy.security.session;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
public final class RefreshTokenHasher {
	private final byte[] secret;
	
	public RefreshTokenHasher(String secret) {
		if (secret == null || secret.isBlank()) {
			throw new IllegalArgumentException("refresh token hash secret must not be blank");
		}
		this.secret = secret.getBytes(StandardCharsets.UTF_8);
	}
	
	/** HMAC-SHA256(tokenRaw) -> base64url string */
	public String hash(String tokenRaw) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret, "HmacSHA256"));
			byte[] out = mac.doFinal(tokenRaw.getBytes(StandardCharsets.UTF_8));
			return Base64.getUrlEncoder().withoutPadding().encodeToString(out);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to hash refresh token", e);
		}
	}
	
	/** constant-time compare (tránh timing attack) */
	public boolean constantTimeEquals(String a, String b) {
		if (a == null || b == null) return false;
		byte[] x = a.getBytes(StandardCharsets.UTF_8);
		byte[] y = b.getBytes(StandardCharsets.UTF_8);
		if (x.length != y.length) return false;
		
		int r = 0;
		/**
		 * 			//x[i] ^ y[i] (Phép XOR):
		 * 			//r |= ... (Phép OR bitwise):
		 * 			//Chỉ cần một lần duy nhất phép XOR trả về kết quả khác 0 (có sự khác biệt),
		 * 			//thì r sẽ trở thành một số khác 0 và "dính" luôn ở đó cho đến hết vòng lặp.
		 * 			// ý nghĩa đã có kết quả nhưng không dừng vòng lặp để cho luôn chạy đử số vòng
		 * 			// từ đó hacker chỉ thấy time phản hồi giống nhau nên khó đoán được họ đã nhập đúng
		 * 			// bao nhiêu ký tự đầu tiên => khó dò mật khẩu ...
		 * 		Nếu r vẫn bằng 0: Nghĩa là tất cả các cặp byte x[i] và y[i] đều giống nhau => Trả
		 * 		về true.
		 * 		Nếu r khác 0: Nghĩa là đã có ít nhất một cặp byte khác nhau => Trả về false.
		 * */
		for (int i = 0; i < x.length; i++) {
			r |= (x[i] ^ y[i]);
		}
		return r == 0;// so sánh r ép về boolean
	}
	
	public String secretFingerprint() {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] digest = md.digest(secret);
			return Base64.getUrlEncoder().withoutPadding()
					.encodeToString(digest)
					.substring(0, 12);
		} catch (Exception e) {
			return "err";
		}
	}
}
