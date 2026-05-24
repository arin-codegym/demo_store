package com.quochuy.ai.rag.vision;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface VisionTextExtractor {
	String extractTextFromImage(MultipartFile file);
	String extractText(Path path, String mimeType, String fileName);
}
