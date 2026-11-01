package com.antonio.sportstournament.registration.dto;

import com.antonio.sportstournament.registration.RegistrationStatus;
import com.antonio.sportstournament.tournament.TournamentStatus;

import java.time.LocalDateTime;

/**
 * JSON returned for one registration (one row of tournament_teams).
 * Includes names so the client can show them directly, and the team's manager
 * so the client knows who may withdraw the registration.
 */
public record RegistrationResponse(
        Long id,
        Long tournamentId,
        String tournamentName,
        TournamentStatus tournamentStatus,
        Long teamId,
        String teamName,
        Long managerId,
        String managerName,
        RegistrationStatus status,
        LocalDateTime registeredAt
) {
}