package com.antonio.sportstournament.tournament.dto;

import com.antonio.sportstournament.tournament.TournamentStatus;
import jakarta.validation.constraints.NotNull;

/**
 * JSON body of PUT /api/tournaments/{id}/status, e.g. {"status":"OPEN"}
 */
public record TournamentStatusRequest(
        @NotNull
        TournamentStatus status
) {
}