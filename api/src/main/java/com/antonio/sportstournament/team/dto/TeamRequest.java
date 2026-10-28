package com.antonio.sportstournament.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * JSON body for creating / updating a team.
 */
public record TeamRequest(
        @NotBlank @Size(max = 100)
        String teamName,

        // TEMPORARY until JWT login: id of the Team Manager who manages this team
        @NotNull
        Long managerId
) {
}