package com.quochuy.ai.rag.ingestion;

import com.quochuy.ai.rag.mapper.RagDocumentMapper;
import com.quochuy.ai.rag.enums.RagAssistantCode;
import com.quochuy.ai.rag.model.RagDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RagDocumentWriteService {
	
	private final RagDocumentMapper ragDocumentMapper;
	

	@Transactional
	public Long createPendingDocument(RagAssistantCode assistantCode,
									  Long importJobId,
									  MultipartFile file,
									  String sourceUri,
									  String checksumSha256,
									  String actor) {
		RagDocument doc = new RagDocument();
		doc.setDocumentCode(buildDocumentCode(file));
		doc.setTitle(resolveTitle(file));
		doc.setFileName(file.getOriginalFilename());
		doc.setFileExt(extensionOf(file.getOriginalFilename()));
		doc.setMimeType(file.getContentType());
		doc.setFileSize(file.getSize());
		doc.setSourceType("UPLOAD");
		doc.setSourceUri(sourceUri);
		doc.setChecksumSha256(checksumSha256);
		doc.setLanguageCode("vi");
		doc.setStatus("PROCESSING");
		doc.setMetadataJson("{}");
		doc.setRawText(null);
		doc.setPageCount(null);
		doc.setImportedAt(null);
		doc.setCreatedBy(actor);
		doc.setUpdatedBy(actor);
		doc.setDeleted(false);
		doc.setImportJobId(importJobId);
		doc.setAssistantCode(assistantCode.name());
		
		ragDocumentMapper.insert(doc);
		return doc.getId();
	}
	
	private String buildDocumentCode(MultipartFile file) {
		return "DOC_" + System.currentTimeMillis() + "_" +
				UUID.randomUUID().toString().replace("-", "").toUpperCase();
	}
	
	private String resolveTitle(MultipartFile file) {
		return file.getOriginalFilename() != null ? file.getOriginalFilename() : "Untitled";
	}
	
	private String extensionOf(String fileName) {
		if (fileName == null || !fileName.contains(".")) {
			return null;
		}
		return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
	}
}
