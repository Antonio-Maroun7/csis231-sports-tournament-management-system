package com.antonio.sportstournamentclient.auth.dto;

/**
 * Answer of POST /api/auth/login and /api/auth/verify-2fa: the logged-in user, kept in the Session.
 * role = ADMIN | ORGANIZER | TEAM_MANAGER
 * token = the JWT sent as "Authorization: Bearer ..." (kept in memory only).
 */
public record LoginResponse(
        Long id,
        String name,
        String email,
        String role,
        boolean twoFactorEnabled,
        String token
) {
}
