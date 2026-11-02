package com.antonio.sportstournament.auth.dto;

import java.time.LocalDateTime;

/**
 * Result of {@code POST /api/auth/login} and {@code POST /api/auth/resend-2fa}: either a completed login or an email 2FA challenge.
 *
 * @param twoFactorRequired true if a code was emailed and must be verified; false if {@code user} holds the finished login
 * @param challengeToken    id to send back with the code; null when no 2FA is required
 * @param maskedEmail       email with the middle hidden (for example {@code ab***@mail.com}); null when no 2FA is required
 * @param codeExpiresAt     when the emailed code stops being valid; null when no 2FA is required
 * @param user              the user with a JWT when no 2FA is required; null while a code is pending
 */
public record AuthResult(
        boolean twoFactorRequired,
        String challengeToken,
        String maskedEmail,
        LocalDateTime codeExpiresAt,
        LoginResponse user
) {
}
