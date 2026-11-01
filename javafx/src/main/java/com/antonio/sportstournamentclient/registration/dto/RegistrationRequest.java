package com.antonio.sportstournamentclient.registration.dto;

/**
 * Body of POST /api/tournaments/{tournamentId}/registrations, e.g. {"teamId": 3}.
 */
public record RegistrationRequest(Long teamId) {
}
