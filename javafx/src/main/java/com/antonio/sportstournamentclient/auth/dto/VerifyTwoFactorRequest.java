package com.antonio.sportstournamentclient.auth.dto;

/**
 * JSON body of POST /api/auth/verify-2fa: {"challengeToken": "...", "code": "482913"}.
 */
public record VerifyTwoFactorRequest(String challengeToken, String code) {
}
