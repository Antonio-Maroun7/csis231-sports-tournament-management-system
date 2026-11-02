package com.antonio.sportstournament.tournament.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * JSON body for creating / editing a tournament.
 * The status is NOT here: it changes only through PUT /api/tournaments/{id}/status.
 * The organizer is the logged-in caller, never sent by the client.
 */
public record TournamentRequest(
        @NotBlank @Size(max = 120)
        String name,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate
) {
}