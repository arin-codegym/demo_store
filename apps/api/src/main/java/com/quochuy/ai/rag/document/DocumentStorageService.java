package com.quochuy.ai.rag.document;

import com.quochuy.ai.rag.document.dto.StoredFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * Storage boundary for uploaded RAG source files.
 *
 * The import pipeline depends on this contract instead of a local filesystem
 * path so storage can move later without changing import orchestration.
 */
public interface DocumentStorageService {
	StoredFile store(MultipartFile file);
}
