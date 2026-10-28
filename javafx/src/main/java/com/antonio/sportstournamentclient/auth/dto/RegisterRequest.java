package com.antonio.sportstournamentclient.auth.dto;

/**
 * Body of POST /api/auth/register (the server always creates a TEAM_MANAGER).
 */
public record RegisterRequest(String name, String email, String password) {
}
