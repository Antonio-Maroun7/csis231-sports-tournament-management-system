package com.antonio.sportstournament.player.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * JSON body for adding / editing a player. The team comes from the URL, not the body.
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