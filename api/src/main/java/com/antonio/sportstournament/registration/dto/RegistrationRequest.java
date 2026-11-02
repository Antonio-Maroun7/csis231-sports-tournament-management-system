package com.antonio.sportstournament.registration.dto;

import jakarta.validation.constraints.NotNull;

/**
 * JSON body of {@code POST /api/tournaments/{tournamentId}/registrations}, e.g. {@code {"teamId": 3}}.
 * The tournament comes from the URL.
 *
 * @param teamId required; id of the team to register (must be managed by the caller, unless the caller is an ADMIN)
 */
public record RegistrationRequest(
        @NotNull
        Long teamId
) {
}