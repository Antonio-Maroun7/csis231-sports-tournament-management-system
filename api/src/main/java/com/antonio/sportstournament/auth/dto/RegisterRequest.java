package com.antonio.sportstournament.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body of {@code POST /api/auth/register}. The new account is always a TEAM_MANAGER.
 *
 * @param name     required, max 100 characters
 * @param email    required, valid email format, max 255 characters
 * @param password required, 6 to 100 characters
 */
public record RegisterRequest(
        @NotBlank @Size(max = 100)
        String name,

        @NotBlank @Email @Size(max = 255)
        String email,

        @NotBlank @Size(min = 6,max = 100)
        String password
) {





}
