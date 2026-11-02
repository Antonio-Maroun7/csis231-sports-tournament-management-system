package com.antonio.sportstournament.auth.dto;

import java.time.LocalDateTime;

/** Result of a login attempt, either a completed login or an email 2FA challenge. */
public record AuthResult(
        boolean twoFactorRequired,
        String challengeToken,
        String maskedEmail,
        LocalDateTime codeExpiresAt,
        LoginResponse user
) {
}
