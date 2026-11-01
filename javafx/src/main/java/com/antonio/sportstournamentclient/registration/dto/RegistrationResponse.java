package com.antonio.sportstournamentclient.registration.dto;

import java.time.LocalDateTime;

/**
 * One registration (one row of tournament_teams) as returned by the API.
 * status = PENDING | APPROVED | REJECTED; tournamentStatus = DRAFT | OPEN | IN_PROGRESS | COMPLETED
 */
public record RegistrationResponse(
        Long id,
        Long tournamentId,
        String tournamentName,
        String tournamentStatus,
        Long teamId,
        String teamName,
        Long managerId,
        String managerName,
        String status,
        LocalDateTime registeredAt
) {
}
