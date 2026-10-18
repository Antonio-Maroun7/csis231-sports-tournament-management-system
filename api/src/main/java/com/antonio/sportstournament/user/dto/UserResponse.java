package com.antonio.sportstournament.user.dto;

import com.antonio.sportstournament.user.Role;

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
