package com.hnigrowth.auth;

import com.hnigrowth.auth.dto.AuthResponse;
import com.hnigrowth.auth.dto.LoginRequest;
import com.hnigrowth.auth.dto.RegisterRequest;
import com.hnigrowth.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok("Registered", authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok("Authenticated", authService.login(req));
    }
}
