package com.quochuy.utils;

import java.text.Normalizer;
import java.util.Locale;

public final class TextNormalizer {
	private TextNormalizer() {
	}
	
	public static String normalizeQuestion(String input) {
		if (input == null) {
			return "";
		}
		
		String value = input.trim().toLowerCase(Locale.ROOT);
		
		
		value = Normalizer.normalize(value, Normalizer.Form.NFD)// Loại bỏ dấu(Tiếng việt) ví dụ
				// chữ á sẽ bị tách thành chữ a và dấu sắc ´
				.replaceAll("\\p{M}", "");//(Regex) \p{M} sẽ tìm tất cả các ký tự thuộc nhóm "Mark"
		
		value = value.replaceAll("[^a-z0-9\\s]", " ");//Xóa ký tự đặc biệt
		value = value.replaceAll("\\s+", " ")//Regex \s+ tìm tất cả các cụm có từ 1 khoảng trắng trở lên và gom chúng lại thành đúng 1 khoảng trắng duy nhất.
				.trim();
		
		return value;
	}
}
