package com.antonio.sportstournamentclient.auth.dto;

/**
 * Answer of POST /api/auth/login: the logged-in user, kept in the Session.
 * role = ADMIN | ORGANIZER | TEAM_MANAGER
 */
public record LoginResponse(
        Long id,
        String name,
        String email,
        String role,
        boolean twoFactorEnabled
) {
}
