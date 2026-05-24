package com.quochuy.ai.rag.controller;

import com.quochuy.ai.rag.enums.RagAssistantCode;
import com.quochuy.ai.rag.importjob.RagImportService;
import com.quochuy.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backend")
public class RagAdminController {
	
	private final RagImportService ragImportService;
	
	@PostMapping(value = "/admin/rag/documents/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> importDocument(
			@RequestParam(value = "assistantCode", required = false) RagAssistantCode assistantCode,// String đơn giản thì dùng RequestParam
//			@RequestParam("operator") String operator,// String đơn giản thì dùng RequestParam
			@RequestPart("file") MultipartFile file,// File thì dùng RequestPart cho đúng ngữ nghĩa
			@AuthenticationPrincipal CustomUserDetails prical) {
		Long documentId = ragImportService.importFile(assistantCode,file,
														 prical.getUserId().toString());
		return ResponseEntity.ok(Map.of("success",true,
										"documentId",documentId,
										"message","Insert success"));
	}
}
