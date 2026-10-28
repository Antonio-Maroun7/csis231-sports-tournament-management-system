package com.antonio.sportstournamentclient.team.dto;

import java.time.LocalDateTime;

/**
 * One team as returned by /api/teams.
 */
public record TeamResponse(
        Long id,
        String teamName,
        Long managerId,
        String managerName,
        LocalDateTime createdAt
) {
}
