package com.antonio.sportstournamentclient.tournament.dto;

/**
 * Body of PUT /api/tournaments/{id}/status, e.g. {"status":"OPEN"}.
 */
public record TournamentStatusRequest(String status) {
}
