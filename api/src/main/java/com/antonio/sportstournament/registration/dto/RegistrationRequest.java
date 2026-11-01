package com.antonio.sportstournament.registration.dto;

import jakarta.validation.constraints.NotNull;

/**
 * JSON body of POST /api/tournaments/{tournamentId}/registrations, e.g. {"teamId": 3}.
 * The tournament comes from the URL.
 */
public record RegistrationRequest(
        @NotNull
        Long teamId
) {
}