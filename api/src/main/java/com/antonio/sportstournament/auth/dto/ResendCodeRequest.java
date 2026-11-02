package com.antonio.sportstournament.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body of {@code POST /api/auth/resend-2fa}: replace an existing email 2FA challenge with a newly mailed code.
 *
 * @param challengeToken required; the token returned by login
 */
public record ResendCodeRequest(@NotBlank String challengeToken) {
}
