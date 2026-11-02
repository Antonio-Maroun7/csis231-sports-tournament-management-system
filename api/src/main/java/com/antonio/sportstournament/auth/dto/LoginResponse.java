package com.antonio.sportstournament.auth.dto;

import com.antonio.sportstournament.user.Role;

import java.time.LocalDateTime;

public record LoginResponse(
        Long id,
        String name,
        String email,
        String role,
        boolean twoFactorEnabled,
        // JWT for the Authorization header; null while a 2FA code is still required
        String token
) {
}
