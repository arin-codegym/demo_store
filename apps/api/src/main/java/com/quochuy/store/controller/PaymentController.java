package com.quochuy.store.controller;

import com.quochuy.store.dto.PaymentRequest;
import com.quochuy.store.dto.PaymentResponse;
import com.quochuy.store.service.impl.PaymentServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/backend/payment")
@RequiredArgsConstructor
public class PaymentController {
	private final PaymentServiceImpl paymentServiceImpl;
	@PostMapping("/create-session")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<PaymentResponse> createSession(
			@Valid @RequestBody PaymentRequest request) {
		PaymentResponse paymentResponse = paymentServiceImpl.createCheckoutSession(
				request);
		return ResponseEntity.ok(paymentResponse);
	}
	
	@PostMapping("/webhook")
	public ResponseEntity<String> handleWebhook(@RequestBody String payload,
												@RequestHeader("Stripe-Signature") String sigHeader) {
		return paymentServiceImpl.handleWebhook(payload, sigHeader);
	}
	
	@GetMapping("/verify")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> verifyPayment(
			@RequestParam String sessionId
	) {
		return paymentServiceImpl.verifyPayment(sessionId);
	}
}
