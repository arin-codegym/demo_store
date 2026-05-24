package com.quochuy.ai.rag.document.extract;

import com.quochuy.ai.rag.exception.RagImportException;
import com.quochuy.ai.rag.document.dto.ExtractedDocument;
import com.quochuy.ai.rag.document.dto.StoredFile;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class PlainTextDocumentTextExtractor implements DocumentTextExtractor {
	@Override
	public boolean supports(String mimeType, String fileName) {
		if (mimeType != null && mimeType.startsWith("text/")) {
			return true;
		}
		if (fileName == null) {
			return false;
		}
		String lowerFileName = fileName.toLowerCase();
		return lowerFileName.endsWith(".md") || lowerFileName.endsWith(
				".txt") || lowerFileName.endsWith(".csv");
	}
	
	@Override
	public ExtractedDocument extract(StoredFile file) {
		Path path = Path.of(file.getStoragePath());
		if (!Files.exists(path)) {
			throw new RagImportException("TEXT_FILE_NOT_FOUND", "Stored text file does not exist");
		}
		try {
			String text = Files.readString(path, StandardCharsets.UTF_8);
			text = normalize(text);
			if (text.isBlank()) {
				throw new RagImportException("TEXT_EXTRACT_EMPTY", "Extracted plain text is empty");
			}
			return ExtractedDocument.builder().text(text).extractorType("PLAIN_TEXT").pageCount(1)
					.metadataJson("{\"extractor\":\"PLAIN_TEXT\"}").build();
		} catch (RagImportException e) {
			throw e;
		} catch (Exception e) {
			throw new RagImportException("TEXT_EXTRACT_FAILED", "Cannot extract plain text", e);
		}
	}
	
	private String normalize(String input) {
		return input == null ? "" : input.replace("\r\n", "\n").trim();
	}
}
