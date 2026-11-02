package com.antonio.sportstournament.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Request containing the challenge token and six-digit email verification code. */
public record VerifyTwoFactorRequest(
        @NotBlank String challengeToken,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "must be exactly 6 digits") String code
) {
}
