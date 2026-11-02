package com.antonio.sportstournament.tournament.dto;

import com.antonio.sportstournament.tournament.TournamentStatus;
import jakarta.validation.constraints.NotNull;

/**
 * JSON body of PUT /api/tournaments/{id}/status, e.g. {@code {"status":"OPEN"}}
 *
 * @param status required; the new status, which must be the one right after the current status
 */
public record TournamentStatusRequest(
        @NotNull
        TournamentStatus status
) {
}