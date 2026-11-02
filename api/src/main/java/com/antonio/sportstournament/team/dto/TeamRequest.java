package com.antonio.sportstournament.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * JSON body for {@code POST /api/teams} (create) and {@code PUT /api/teams/{id}} (update).
 * The manager is the logged-in caller, never sent by the client.
 *
 * @param teamName required (not blank), max 100 characters
 */
public record TeamRequest(
        @NotBlank @Size(max = 100)
        String teamName
) {
}