package com.antonio.sportstournamentclient.user.dto;

/**
 * Body of POST /api/users (Administrator creates any kind of account).
 * role = ADMIN | ORGANIZER | TEAM_MANAGER
 */
public record UserCreateRequest(String name, String email, String password, String role) {
}
