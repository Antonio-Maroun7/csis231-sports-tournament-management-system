package com.antonio.sportstournament.team.dto;

import java.time.LocalDateTime;

/**
 * JSON returned to the client for one team.
 */
public record TeamResponse(
        Long id,
        String teamName,
        Long managerId,
        String managerName,
        LocalDateTime createdAt
) {
}