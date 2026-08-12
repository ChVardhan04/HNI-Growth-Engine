package com.hnigrowth.auth.dto;

public record AuthResponse(
        Long id,
        String token,
        String tokenType,
        String fullName,
        String email,
        String role
) {}
