package com.antonio.sportstournament.auth.dto;

import com.antonio.sportstournament.user.Role;

import java.time.LocalDateTime;

public record LoginResponse(
        Long id,
        String name,
        String email,
        String role,
        boolean twoFactorEnabled
) {
}
