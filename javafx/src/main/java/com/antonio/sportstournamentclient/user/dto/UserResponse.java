package com.antonio.sportstournamentclient.user.dto;

import java.time.LocalDateTime;

/**
 * One account as returned by /api/users.
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        String role,
        boolean twoFactorEnabled,
        LocalDateTime createdAt
) {
}
