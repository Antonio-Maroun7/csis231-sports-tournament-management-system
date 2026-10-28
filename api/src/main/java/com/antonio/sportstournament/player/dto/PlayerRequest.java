package com.antonio.sportstournament.player.dto;

import jakarta.validation.constraints.*;

public record PlayerRequest(
        @NotBlank @Size(max=50)
        String firstName,


        @NotBlank @Size(max=50)
        String lastName,

        @NotNull @Min(0) @Max(99)
        Integer jerseyNumber,

        @NotBlank @Size(max=30)
        String position
) {
}
