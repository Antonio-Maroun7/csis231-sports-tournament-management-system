package com.antonio.sportstournament.user.dto;

import com.antonio.sportstournament.user.Role;

import java.time.LocalDateTime;

/**
 * Response of the {@code /api/users} endpoints. It never contains the password hash.
 *
 * @param id               user id
 * @param name             display name
 * @param email            login email (lower case)
 * @param role             ADMIN, ORGANIZER or TEAM_MANAGER
 * @param twoFactorEnabled true if login needs the emailed 6-digit code
 * @param createdAt        when the account was created
 */
public record UserResponse(

        Long id,
        String name,
        String email,
        Role role,
        boolean twoFactorEnabled,
        LocalDateTime createdAt
) {
}
