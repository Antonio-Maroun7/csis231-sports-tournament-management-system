package com.antonio.sportstournament.auth.dto;

import com.antonio.sportstournament.user.Role;

import java.time.LocalDateTime;

/**
 * The logged-in user returned by register, login (without 2FA) and verify-2fa.
 *
 * @param id               user id
 * @param name             display name
 * @param email            login email
 * @param role             role name: ADMIN, ORGANIZER or TEAM_MANAGER
 * @param twoFactorEnabled true if this user logs in with an emailed code
 * @param token            JWT for the {@code Authorization: Bearer} header; null while a 2FA code is still required
 */
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
