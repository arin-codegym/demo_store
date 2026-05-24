package com.quochuy.ai.rag.document.extract;

import com.quochuy.ai.rag.document.dto.ExtractedDocument;
import com.quochuy.ai.rag.document.dto.StoredFile;

/**
 * Pluggable text extractor for imported RAG documents.
 *
 * RagImportService injects all implementations and picks the first extractor
 * whose supports method matches the uploaded file.
 */
public interface DocumentTextExtractor {
	boolean supports(String mimeType, String fileName);
	ExtractedDocument extract(StoredFile file);
}
