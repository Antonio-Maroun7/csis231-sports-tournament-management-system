package com.antonio.sportstournament.team.dto;

import java.time.LocalDateTime;

/**
 * JSON returned to the client for one team (responses of the {@code /api/teams} endpoints).
 *
 * @param id          team id
 * @param teamName    name of the team
 * @param managerId   id of the user who manages the team
 * @param managerName display name of that manager
 * @param createdAt   when the team was created
 */
public record TeamResponse(
        Long id,
        String teamName,
        Long managerId,
        String managerName,
        LocalDateTime createdAt
) {
}