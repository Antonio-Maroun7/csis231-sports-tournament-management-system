package com.antonio.sportstournament.user.dto;

import com.antonio.sportstournament.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body of {@code POST /api/users} (ADMIN only).
 *
 * @param name     required, max 100 characters
 * @param email    required, valid email format, max 255 characters
 * @param password required, 6 to 100 characters (stored only as a hash)
 * @param role     required: ADMIN, ORGANIZER or TEAM_MANAGER
 */
public record UserCreateRequest(

        @NotBlank @Size(max = 100)
        String name,

        @NotBlank @Email  @Size(max = 255)
        String email,

        @NotBlank @Size(min = 6,max = 100)
        String password,

        @NotNull
        Role role
) {
}
