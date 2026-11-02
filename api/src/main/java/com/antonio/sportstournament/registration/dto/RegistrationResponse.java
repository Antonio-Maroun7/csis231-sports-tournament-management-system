package com.antonio.sportstournament.registration.dto;

import com.antonio.sportstournament.registration.RegistrationStatus;
import com.antonio.sportstournament.tournament.TournamentStatus;

import java.time.LocalDateTime;

/**
 * JSON returned for one registration (one row of tournament_teams).
 * Includes names so the client can show them directly, and the team's manager
 * so the client knows who may withdraw the registration.
 *
 * @param id               registration id
 * @param tournamentId     id of the tournament
 * @param tournamentName   name of the tournament
 * @param tournamentStatus current status of the tournament (registrations are only possible while OPEN)
 * @param teamId           id of the registered team
 * @param teamName         name of the registered team
 * @param managerId        id of the team's manager
 * @param managerName      display name of the team's manager
 * @param status           PENDING, APPROVED or REJECTED
 * @param registeredAt     when the team registered
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