package com.antonio.sportstournamentclient.auth.dto;

/**
 * JSON body of POST /api/auth/resend-2fa: {"challengeToken": "..."}.
 */
public record ResendCodeRequest(String challengeToken) {
}
