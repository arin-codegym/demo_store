package com.quochuy.store.controller;

import com.quochuy.store.dto.OrderDto;
import com.quochuy.helper.DatabaseExceptionUtil;
import com.quochuy.security.CustomUserDetails;
import com.quochuy.store.service.impl.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/api/backend")
@RequiredArgsConstructor
public class OrderController {
	private final OrderServiceImpl orderServiceImpl;
	private final DatabaseExceptionUtil databaseExceptionUtil;
	
	@PostMapping("/order/create")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<OrderDto> createOrder(
			@RequestHeader("Idempotency-Key") String key,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		try {
			OrderDto orderDto = orderServiceImpl.createOrder(
					userDetails.getUserId(), userDetails.getEmail(), key);
			return ResponseEntity.ok(orderDto);
		} catch (DataIntegrityViolationException e) {
			Throwable root = e.getRootCause();
//				String sqlState = psql.getSQLState();  // 23505 = unique violation
//				if ("23505".equals(
//						sqlState) && psql.getMessage()
//						.contains(
//								"uk_orders_idempotency_key")) { // chỉ check idempotency_key còn lỗi khác thì throw lỗi để xác định chính xác lỗi
//					return ResponseEntity.ok(
//							orderService.exitsOrder(key));
//				}
			if (databaseExceptionUtil.isDuplicateKey(e,
													 "uk_orders_idempotency_key")) {
				return ResponseEntity.ok(orderServiceImpl.exitsOrder(key));
			}
			throw e;
		}
	}
	
	@GetMapping("/orders/is-paid")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> fetchOrder(
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		return ResponseEntity.ok(Map.of("orders",
										orderServiceImpl.getPaidOrderByUserId(userDetails.getUserId()))
				);
	}
}
