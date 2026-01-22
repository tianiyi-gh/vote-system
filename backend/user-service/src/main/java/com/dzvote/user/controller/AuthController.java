package com.dzvote.user.controller;

import com.dzvote.user.dto.LoginRequest;
import com.dzvote.user.dto.LoginResponse;
import com.dzvote.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "认证相关接口")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("登录失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        try {
            authService.logout(token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("登出失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Service is running");
    }
}
