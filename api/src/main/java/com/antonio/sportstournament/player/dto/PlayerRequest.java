package com.antonio.sportstournament.player.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * JSON body for {@code POST /api/teams/{teamId}/players} (add) and {@code PUT /api/players/{id}} (edit).
 * The team comes from the URL, not the body.
 *
 * @param firstName    required (not blank), max 50 characters
 * @param lastName     required (not blank), max 50 characters
 * @param jerseyNumber required, between 0 and 99; must be unique inside the team (checked by the service)
 * @param position     optional, max 30 characters (e.g. Goalkeeper, Defender, Midfielder, Forward); blank is stored as NULL
 */
public record PlayerRequest(
        @NotBlank @Size(max = 50)
        String firstName,

        @NotBlank @Size(max = 50)
        String lastName,

        @NotNull @Min(0) @Max(99)
        Integer jerseyNumber,

        // optional: Goalkeeper, Defender, Midfielder, Forward...
        @Size(max = 30)
        String position
) {
}