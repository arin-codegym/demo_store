package com.quochuy.ai.rag.vision;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class GeminiVisionTextExtractor implements VisionTextExtractor {
	
	private final ChatClient.Builder chatClientBuilder;
	
	@Override
	public String extractTextFromImage(MultipartFile file) {
		ChatClient chatClient = chatClientBuilder.build();
		try {
			MimeType mimeType = resolveMimeType(file);
			byte[] imageBytes = file.getBytes();
			ByteArrayResource resource = new ByteArrayResource(imageBytes) {
				@Override
				public String getFilename() {
					return file.getOriginalFilename();
				}
			};
			
			String response = chatClient.prompt()
					.user(u -> u
							.text("""
                                  Hãy trích xuất toàn bộ văn bản nhìn thấy trong ảnh.
                                  Chỉ trả về nội dung text đọc được.
                                  Giữ xuống dòng hợp lý.
                                  Không giải thích, không tóm tắt, không suy diễn.
                                  Nếu có bảng đơn giản, hãy chuyển thành text dễ đọc.
                                  """)
							.media(mimeType, resource))
					.call()
					.content();
			
			if (response == null || response.isBlank()) {
				throw new RuntimeException("Vision model returned empty text");
			}
			
			return normalize(response);
		} catch (Exception e) {
			throw new RuntimeException("Extract image text by AI failed", e);
		}
	}
	
	@Override
	public String extractText(Path path, String mimeType, String fileName) {
		ChatClient chatClient = chatClientBuilder.build();
		
		String response = chatClient.prompt()
				.user(u -> u
						.text("""
                              Hãy trích xuất toàn bộ văn bản nhìn thấy trong ảnh.
                              Chỉ trả về nội dung text đọc được.
                              Không giải thích, không tóm tắt, không suy diễn.
                              """)
						.media(MimeType.valueOf(mimeType), new FileSystemResource(path) {
							@Override
							public String getFilename() {
								return fileName;
							}
						}))
				.call()
				.content();
		
		return normalize(response);
	}
	
	private MimeType resolveMimeType(MultipartFile file) {
		String contentType = file.getContentType();
		if (contentType == null || contentType.isBlank()) {
			return MimeTypeUtils.IMAGE_JPEG;
		}
		return MimeType.valueOf(contentType);
	}
	
	private String normalize(String text) {
		return text == null ? "" :
				text.replace("\r\n", "\n")
						.replaceAll("[ \\t]+", " ")
						.replaceAll("\\n{3,}", "\n\n")
						.trim();
	}
}
