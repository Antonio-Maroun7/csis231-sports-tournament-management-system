package com.antonio.sportstournamentclient.auth.dto;

import java.time.LocalDateTime;

/**
 * Answer of POST /api/auth/login and POST /api/auth/resend-2fa.
 *
 * twoFactorRequired = false -> "user" is filled: the user is logged in.
 * twoFactorRequired = true  -> a 6-digit code was emailed; keep "challengeToken" and send it
 *                              with the code to POST /api/auth/verify-2fa.
 */
public record AuthResult(
        boolean twoFactorRequired,
        String challengeToken,
        String maskedEmail,
        LocalDateTime codeExpiresAt,
        LoginResponse user
) {
}
