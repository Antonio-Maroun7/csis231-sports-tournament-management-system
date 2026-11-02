package com.antonio.sportstournament.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Request to replace an existing email 2FA challenge with a newly mailed code. */
public record ResendCodeRequest(@NotBlank String challengeToken) {
}
