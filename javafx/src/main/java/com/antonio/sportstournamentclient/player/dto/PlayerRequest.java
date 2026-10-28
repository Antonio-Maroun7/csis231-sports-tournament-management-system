package com.antonio.sportstournamentclient.player.dto;

/**
 * Body of POST /api/teams/{teamId}/players and PUT /api/players/{id}.
 * jerseyNumber 0-99, unique inside the team (the server answers 409 otherwise).
 */
public record PlayerRequest(String firstName, String lastName, Integer jerseyNumber, String position) {
}
