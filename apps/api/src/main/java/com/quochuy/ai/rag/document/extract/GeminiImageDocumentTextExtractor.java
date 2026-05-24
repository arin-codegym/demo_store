package com.quochuy.ai.rag.document.extract;

import com.quochuy.ai.rag.exception.RagImportException;
import com.quochuy.ai.rag.document.extract.DocumentTextExtractor;
import com.quochuy.ai.rag.document.dto.ExtractedDocument;
import com.quochuy.ai.rag.document.dto.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class GeminiImageDocumentTextExtractor implements DocumentTextExtractor {
	
	private final ChatClient.Builder chatClientBuilder;
	
	@Override
	public boolean supports(String mimeType, String fileName) {
		return mimeType != null && mimeType.startsWith("image/");
	}
	
	@Override
	public ExtractedDocument extract(StoredFile file) {
		Path path = Path.of(file.getStoragePath());
		
		if (!Files.exists(path)) {
			throw new RagImportException("IMAGE_FILE_NOT_FOUND", "Stored image file does not exist");
		}
		try {
			ChatClient chatClient = chatClientBuilder.build();
			MimeType mimeType = MimeType.valueOf(file.getMimeType());
			
			String response = chatClient.prompt()
					.user(u -> u
							.text("""
                                  Hãy trích xuất toàn bộ văn bản nhìn thấy trong ảnh.
                                  Chỉ trả về nội dung text đọc được.
                                  Giữ xuống dòng hợp lý.
                                  Không giải thích, không tóm tắt, không suy diễn.
                                  Nếu có bảng đơn giản thì chuyển thành text dễ đọc.
                                  """)
							.media(mimeType, new FileSystemResource(path) {
								@Override
								public String getFilename() {
									return file.getOriginalFileName();
								}
							}))
					.call()
					.content();
			
			String text = normalize(response);
			if (text.isBlank()) {
				throw new RagImportException("VISION_EMPTY_TEXT", "Vision model returned empty text");
			}
			
			return ExtractedDocument.builder()
					.text(text)
					.extractorType("GEMINI_VISION")
					.pageCount(1)
					.metadataJson("{\"extractor\":\"GEMINI_VISION\"}")
					.build();
			
		}
		catch (RagImportException e) {
			throw e;
		} catch (Exception e) {
			throw new RagImportException("GEMINI_VISION_FAILED", "Gemini vision extraction failed", e);
		}
	}
	
	private String normalize(String input) {
		return input == null ? "" : input.replace("\r\n", "\n").trim();
	}
}
