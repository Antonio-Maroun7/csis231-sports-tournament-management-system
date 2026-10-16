package com.antonio.sportstournament.dto;

import com.antonio.sportstournament.model.Role;

import java.time.LocalDateTime;

public record UserResponse(

        Long id,
        String name,
        String email,
        Role role,
        boolean twoFactorEnabled,
        LocalDateTime createdAt
) {
}
