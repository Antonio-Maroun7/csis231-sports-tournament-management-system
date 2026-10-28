package com.antonio.sportstournamentclient.auth.dto;

import java.time.LocalDateTime;

/**
 * Answer of POST /api/auth/register: the account that was just created.
 */
public record RegisterResponse(
        Long id,
        String name,
        String email,
        String role,
        boolean twoFactorEnabled,
        LocalDateTime createdAt
) {
    /** Registering also logs the user in (like the instructor's client): convert to the Session type. */
    public LoginResponse toLoginResponse() {
        return new LoginResponse(id, name, email, role, twoFactorEnabled);
    }
}
