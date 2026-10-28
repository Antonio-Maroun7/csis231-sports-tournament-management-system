package com.antonio.sportstournamentclient.team.dto;

/**
 * Body of POST /api/teams and PUT /api/teams/{id}.
 * managerId = the Team Manager who owns the team (until JWT login, the client sends it).
 */
public record TeamRequest(String teamName, Long managerId) {
}
