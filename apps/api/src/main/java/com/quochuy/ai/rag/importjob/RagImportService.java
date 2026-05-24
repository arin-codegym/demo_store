package com.quochuy.ai.rag.importjob;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quochuy.ai.embedding.EmbeddingService;
import com.quochuy.ai.rag.mapper.RagDocumentMapper;
import com.quochuy.ai.rag.mapper.RagImportJobMapper;
import com.quochuy.ai.rag.document.DocumentStorageService;
import com.quochuy.ai.rag.document.extract.DocumentTextExtractor;
import com.quochuy.ai.rag.document.chunk.TextChunker;
import com.quochuy.ai.rag.document.dto.ExtractedDocument;
import com.quochuy.ai.rag.document.dto.StoredFile;
import com.quochuy.ai.rag.document.dto.TextChunk;
import com.quochuy.ai.rag.enums.RagAssistantCode;
import com.quochuy.ai.rag.model.RagChunkEntity;
import com.quochuy.ai.rag.model.RagImportJob;
import com.quochuy.ai.rag.ingestion.RagChunkWriteService;
import com.quochuy.ai.rag.ingestion.RagDocumentWriteService;
import com.quochuy.ai.rag.exception.RagImportException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Coordinates the admin RAG document import pipeline.
 *
 * This service owns the import transaction state: job status, document status,
 * extracted text, chunks, and embeddings. File storage, text extraction,
 * chunking, and embedding are delegated to smaller services.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagImportService {
	private final RagImportJobMapper ragImportJobMapper;
	private final RagDocumentMapper ragDocumentMapper;
	private final RagDocumentWriteService ragDocumentWriteService;
	private final RagChunkWriteService ragChunkWriteService;
	private final DocumentStorageService documentStorageService;
	private final List<DocumentTextExtractor> documentTextExtractors;
	private final TextChunker textChunker;
	private final EmbeddingService embeddingService;
	private final ObjectMapper objectMapper;
	
	
	public Long importFile(RagAssistantCode assistantCode, MultipartFile file, String actor) {
		// Create the job first so every failure can be tracked by jobId.
		RagImportJob job = new RagImportJob();
		job.setJobCode(buildJobCode(file));
		job.setFileName(file.getOriginalFilename());
		job.setSourceType("UPLOAD");
		job.setSourceUri(null);
		job.setStatus("PENDING");
		job.setTotalDocuments(1);
		job.setSuccessDocuments(0);
		job.setFailedDocuments(0);
		job.setErrorMessage(null);
		job.setCreatedBy(actor);
		ragImportJobMapper.insert(job);
		Long documentId = null;
		try {
			ragImportJobMapper.markProcessing(job.getId());
			
			StoredFile storedFile = documentStorageService.store(file);
			
			 documentId = ragDocumentWriteService.createPendingDocument(
					assistantCode,
					job.getId(),
					file,
					storedFile.getStoragePath(),
					storedFile.getChecksumSha256(),
					actor
			);
			
			// Multiple extractors are registered; choose the first one that supports the file.
			DocumentTextExtractor extractor = resolveExtractor(
					storedFile.getMimeType(),
					storedFile.getOriginalFileName()
			);
			
			ExtractedDocument extracted = extractor.extract(storedFile);
			
			ragDocumentMapper.updateImportSuccess(
					documentId,
					"IMPORTED",
					extracted.getText(),
					extracted.getPageCount(),
					extracted.getMetadataJson(),
					actor
			);
			
			List<TextChunk> textChunks = textChunker.chunk(extracted.getText());
			if (textChunks == null || textChunks.isEmpty()) {
				throw new RagImportException("TEXT_CHUNK_EMPTY", "No chunks generated");
			}
			
			List<RagChunkEntity> chunkEntities = new ArrayList<>();
			int chunkNo = 0;
			
			for (TextChunk tc : textChunks) {
				// Store both JSON and pgvector literal forms because different queries use each shape.
				float[] embedding = embeddingService.embed(tc.getText());
				
				RagChunkEntity entity = new RagChunkEntity();
				entity.setDocumentId(documentId);
				entity.setChunkNo(chunkNo++);
				entity.setChunkText(tc.getText());
				entity.setChunkTextNorm(normalize(tc.getText()));
				entity.setTokenCount(tc.getEstimatedTokenCount());
				entity.setCharCount(tc.getText() != null ? tc.getText().length() : 0);
				entity.setPageFrom(null);
				entity.setPageTo(null);
				entity.setSectionTitle(null);
				entity.setMetadataJson("{}");
				entity.setEmbeddingJson(toJson(embedding));//toJson và toVectorLiteral tách
				// senmactic chứ chúng dùng chung cho nhau tức không cần phải tạo 2 method
				entity.setEmbeddingVector(toVectorLiteral(embedding)); // pgvector xử lý sau nếu bạn muốn
				chunkEntities.add(entity);
			}
			
			ragChunkWriteService.replaceDocumentChunks(documentId, chunkEntities);
			ragDocumentMapper.updateStatus(documentId, "ACTIVE");
			ragImportJobMapper.markCompleted(job.getId(), 1, 0);
			
			return job.getId();
			
		} catch (Exception e) {
			log.error("RAG import failed, jobId={}", job.getId(), e);
			ragImportJobMapper.markFailed(job.getId(), 1, safeMessage(e));
			if(documentId != null){
				ragDocumentMapper.updateStatus(documentId, "FAILED");
			}
			throw e;
		}
	}
	
	private DocumentTextExtractor resolveExtractor(String mimeType, String fileName) {
		return documentTextExtractors.stream()
				.filter(it -> it.supports(mimeType, fileName))
				.findFirst()
				.orElseThrow(() -> new RagImportException(
						"DOCUMENT_EXTRACTOR_NOT_FOUND",
						"No extractor found for mimeType=" + mimeType + ", fileName=" + fileName
				));
	}
	
	private String buildJobCode(MultipartFile file) {
		String name = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
		return "RAG_JOB_" + System.currentTimeMillis() + "_" +
				UUID.randomUUID().toString().replace("-", "").toUpperCase();
	}
	
	private String normalize(String text) {
		return text == null ? "" : text.replaceAll("\\s+", " ").trim().toLowerCase();
	}
	
	private String toJson(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (Exception e) {
			throw new RagImportException("JSON_SERIALIZE_FAILED", "Cannot serialize json", e);
		}
	}
	private String toVectorLiteral(float[] embedding) {
		if (embedding == null || embedding.length == 0) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder("[");
		for (int i = 0; i < embedding.length; i++) {
			if (i > 0) {
				sb.append(",");
			}
			sb.append(embedding[i]);
		}
		sb.append("]");
		return sb.toString();
	}
	
	private String safeMessage(Exception e) {
		return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
	}
}
