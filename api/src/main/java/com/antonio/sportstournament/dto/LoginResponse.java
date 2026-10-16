package com.antonio.sportstournament.dto;

import com.antonio.sportstournament.model.Role;

import java.time.LocalDateTime;

public record LoginResponse(
        Long id,
        String name,
        String email,
        String role,
        boolean twoFactorEnabled
) {
}
