package com.quochuy.ai.rag.document.extract;

import com.quochuy.ai.rag.exception.RagImportException;
import com.quochuy.ai.rag.document.dto.ExtractedDocument;
import com.quochuy.ai.rag.document.dto.StoredFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class PdfDocumentTextExtractor implements DocumentTextExtractor {
	
	@Override
	public boolean supports(String mimeType, String fileName) {
		return "application/pdf".equalsIgnoreCase(mimeType);
	}
	
	@Override
	public ExtractedDocument extract(StoredFile file) {
		Path path = Path.of(file.getStoragePath());
		if (!Files.exists(path)) {
			throw new RagImportException("PDF_FILE_NOT_FOUND", "Stored PDF file does not exist");
		}
		
//		try (PDDocument document = PDDocument.load(file.getBytes())) {
//			PDFTextStripper stripper = new PDFTextStripper();
		try (PDDocument document = PDDocument.load(new File(file.getStoragePath()))) {
			PDFTextStripper stripper = new PDFTextStripper();
			String text = stripper.getText(document);
			
			if (text == null || text.isBlank()) {
				throw new RagImportException("PDF_EXTRACT_EMPTY", "PDF extracted text is empty");
			}
			
			return ExtractedDocument.builder()
					.text(normalize(text))
					.extractorType("PDFBOX")
					.pageCount(document.getNumberOfPages())
					.metadataJson("{\"extractor\":\"PDFBOX\"}")
					.build();
			
		} catch (RagImportException e) {
			throw e;
		} catch (Exception e) {
			throw new RagImportException("PDF_EXTRACT_FAILED", "Cannot extract text from PDF", e);
		}
	}
	
	private String normalize(String input) {
		return input == null ? "" : input.replace("\r\n", "\n").trim();
	}
}
