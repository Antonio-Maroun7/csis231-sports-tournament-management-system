package com.antonio.sportstournamentclient.team.dto;

/**
 * Body of POST /api/teams and PUT /api/teams/{id}.
 * The manager is the logged-in user (taken from the JWT by the server).
 */
public record TeamRequest(String teamName) {
}
