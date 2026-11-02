package com.antonio.sportstournament.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request body of {@code POST /api/auth/verify-2fa}: the challenge token and six-digit email verification code.
 *
 * @param challengeToken required; the token returned by login
 * @param code           required, exactly 6 digits
 */
public record VerifyTwoFactorRequest(
        @NotBlank String challengeToken,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "must be exactly 6 digits") String code
) {
}
