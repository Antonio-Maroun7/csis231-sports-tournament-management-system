package com.antonio.sportstournament.tournament.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * JSON body for {@code POST /api/tournaments} (create) and {@code PUT /api/tournaments/{id}} (edit).
 * The status is NOT here: it changes only through PUT /api/tournaments/{id}/status.
 * The organizer is the logged-in caller, never sent by the client.
 *
 * @param name      required (not blank), max 120 characters
 * @param startDate required, ISO date (yyyy-MM-dd)
 * @param endDate   required, ISO date; must not be before startDate (checked by the service, 400)
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