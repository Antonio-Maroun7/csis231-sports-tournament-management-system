package com.antonio.sportstournament.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * JSON body for creating / updating a team. The manager is the logged-in caller, never sent by the client.
 */
public record TeamRequest(
        @NotBlank @Size(max = 100)
        String teamName
) {
}