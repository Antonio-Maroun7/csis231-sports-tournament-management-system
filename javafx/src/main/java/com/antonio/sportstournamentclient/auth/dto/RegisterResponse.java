package com.antonio.sportstournamentclient.auth.dto;

/**
 * Answer of POST /api/auth/register: the account that was just created, plus its JWT.
 */
public record RegisterResponse(
        Long id,
        String name,
        String email,
        String role,
        boolean twoFactorEnabled,
        String token
) {
    /** Registering also logs the user in (like the instructor's client): convert to the Session type. */
    public LoginResponse toLoginResponse() {
        return new LoginResponse(id, name, email, role, twoFactorEnabled, token);
    }
}
