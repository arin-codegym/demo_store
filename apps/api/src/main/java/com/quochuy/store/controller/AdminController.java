package com.quochuy.store.controller;

import com.quochuy.store.dto.request.CreateUserRequestBody;
import com.quochuy.store.dto.request.UpdateUserRequestBody;
import com.quochuy.security.CustomUserDetails;
import com.quochuy.store.service.impl.AdminServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/backend/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminServiceImpl adminServiceImpl;

    /**
     * @PreAuthorize là lớp bảo vệ 2.
     * Ngay cả khi URL Filter bị cấu hình sai, hàm này vẫn kiểm tra quyền của JWT.
     */
    @GetMapping("/dashboard-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDashboardStats() {
        // Giả lập dữ liệu nhạy cảm chỉ Admin mới thấy
        return ResponseEntity.ok(Map.of(
                "totalRevenue", 50000000,
                "totalUsers", 1200,
                "message", "Chào mừng Admin trở lại!"
        ));
    }
    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDashboardOrder() {
        // Giả lập dữ liệu nhạy cảm chỉ Admin mới thấy
        return ResponseEntity.ok(adminServiceImpl.getDashboardOrder());
    }
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDashboardUsers() {
        // Giả lập dữ liệu nhạy cảm chỉ Admin mới thấy
        return ResponseEntity.ok(adminServiceImpl.getDashboardUsers());
    }
    
    @PutMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable("userId") String userId ,
                                        @RequestBody UpdateUserRequestBody updateUserRequestBody,
                                        @AuthenticationPrincipal CustomUserDetails principal) {
        adminServiceImpl.updateUser(UUID.fromString(userId), updateUserRequestBody);
        return ResponseEntity.ok(Map.of("message","Update success"));
    }

    @PostMapping("/users/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequestBody createUserRequestBody) {
        adminServiceImpl.createUser(createUserRequestBody);
        return ResponseEntity.noContent().build();
    }
}
