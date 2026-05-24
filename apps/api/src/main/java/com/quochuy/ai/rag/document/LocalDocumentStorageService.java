package com.quochuy.ai.rag.document;

import com.quochuy.ai.rag.document.dto.StoredFile;
import com.quochuy.ai.rag.exception.RagImportException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@Service
//public class LocalDocumentStorageService implements DocumentStorageService {
//	private final Path rootDir;
//
//	public LocalDocumentStorageService(
//			@Value("${app.rag.storage.local-root:uploads/rag}") String localRootDir) {
//		this.rootDir = Paths.get(localRootDir).toAbsolutePath().normalize();
//		try {
//			Files.createDirectories(this.rootDir);
//		} catch (IOException e) {
//			throw new IllegalStateException("Cannot create RAG storage directory: " + this.rootDir, e);
//		}
//	}
//
//	@Override
//	public StoredFile store(MultipartFile file) {
//		if (file == null || file.isEmpty()) {
//			throw new RagImportException("FILE_EMPTY", "Uploaded file is empty");
//		}
//
//		try {
//			byte[] bytes = file.getBytes();
//			String checksumSha256 = sha256Hex(bytes);
//
//			String originalFileName = file.getOriginalFilename();
//			String storedFileName = buildStoredFileName(originalFileName);
//			Path targetPath = rootDir.resolve(storedFileName).normalize();
//
//			Files.write(
//					targetPath,
//					bytes,
//					StandardOpenOption.CREATE,
//					StandardOpenOption.TRUNCATE_EXISTING
//			);
//
//			return StoredFile.builder()
//					.storagePath(targetPath.toString())
//					.originalFileName(originalFileName)
//					.mimeType(file.getContentType())
//					.checksumSha256(checksumSha256)
////					.bytes(bytes)
//					.build();
//
//		} catch (IOException e) {
//			throw new RagImportException("FILE_STORE_FAILED", "Cannot store uploaded file", e);
//		}
//	}
//
//	private String buildStoredFileName(String originalFileName) {
//		String ext = extensionOf(originalFileName);
//		String random = UUID.randomUUID().toString().replace("-", "");
//
//		if (ext == null || ext.isBlank()) {
//			return random;
//		}
//		return random + "." + ext;
//	}
//
//	private String extensionOf(String fileName) {
//		if (fileName == null || !fileName.contains(".")) {
//			return null;
//		}
//		return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
//	}
//
//	private String sha256Hex(byte[] data) {
//		try {
//			MessageDigest md = MessageDigest.getInstance("SHA-256");
//			byte[] digest = md.digest(data);
//			return HexFormat.of().formatHex(digest);
//		} catch (NoSuchAlgorithmException e) {
//			throw new IllegalStateException("SHA-256 algorithm not available", e);
//		}
//	}
//}
/**Bản này:
 *
 * validate file
 * tạo tên file lưu
 * copy bằng stream
 * tính SHA-256 trong lúc copy
 * không giữ whole file trong memory*/
public class LocalDocumentStorageService implements DocumentStorageService {
	
	private final Path rootDir;
	
	public LocalDocumentStorageService(
			@Value("${app.rag.storage.local-root:uploads/rag}") String localRootDir
	) {
		this.rootDir = Paths.get(localRootDir).toAbsolutePath().normalize();
	}
	
	@PostConstruct
	public void init() {
		try {
			Files.createDirectories(rootDir);
		} catch (IOException e) {
			throw new RagImportException("STORAGE_INIT_FAILED", "Cannot initialize local storage", e);
		}
	}
	
	@Override
	public StoredFile store(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new RagImportException("FILE_EMPTY", "Uploaded file is empty");
		}
		
		String originalFileName = safeOriginalFileName(file.getOriginalFilename());
		String storedFileName = buildStoredFileName(originalFileName);
		Path targetPath = rootDir.resolve(storedFileName).normalize();
		
		ensureInsideRoot(targetPath);
		
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			
			try (InputStream inputStream = file.getInputStream();
				 DigestInputStream digestInputStream = new DigestInputStream(inputStream, messageDigest)) {
				
				Files.copy(
						digestInputStream,
						targetPath,
						StandardCopyOption.REPLACE_EXISTING
				);
			}
			
			String checksumSha256 = HexFormat.of().formatHex(messageDigest.digest());
			
			return StoredFile.builder()
					.storagePath(targetPath.toString())
					.originalFileName(originalFileName)
					.mimeType(file.getContentType())
					.checksumSha256(checksumSha256)
					.build();
			
		} catch (RagImportException e) {
			throw e;
		} catch (Exception e) {
			throw new RagImportException("FILE_STORE_FAILED", "Cannot store uploaded file", e);
		}
	}
	
	private void ensureInsideRoot(Path targetPath) {
		if (!targetPath.startsWith(rootDir)) {
			throw new RagImportException("INVALID_STORAGE_PATH", "Resolved storage path is invalid");
		}
	}
	
	private String safeOriginalFileName(String originalFileName) {
		if (originalFileName == null || originalFileName.isBlank()) {
			return "upload.bin";
		}
		
		String normalized = Paths.get(originalFileName).getFileName().toString().trim();
		return normalized.isBlank() ? "upload.bin" : normalized;
	}
	
	private String buildStoredFileName(String originalFileName) {
		String extension = extractExtension(originalFileName);
//		String baseName = stripExtension(originalFileName)
//				.replaceAll("[\\\\/:*?\"<>|]", "_")
//				.replaceAll("\\s+", "_");
//
//		if (baseName.isBlank()) {
//			baseName = "file";
//		}
		
		String suffix = UUID.randomUUID().toString().replace("-", "");
//		return extension.isBlank()
//				? baseName + "_" + suffix
//				: baseName + "_" + suffix + "." + extension;
		return extension.isBlank() ? suffix : suffix + "." + extension;
	}
	
	private String extractExtension(String fileName) {
		int lastDot = fileName.lastIndexOf('.');
		if (lastDot < 0 || lastDot == fileName.length() - 1) {
			return "";
		}
		return fileName.substring(lastDot + 1);
	}
	
	private String stripExtension(String fileName) {
		int lastDot = fileName.lastIndexOf('.');
		if (lastDot < 0) {
			return fileName;
		}
		return fileName.substring(0, lastDot);
	}
}
