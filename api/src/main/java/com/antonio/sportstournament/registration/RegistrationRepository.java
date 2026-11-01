package com.antonio.sportstournament.registration;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Data access for the tournament_teams table (team registrations).
 */
public interface RegistrationRepository extends JpaRepository<TournamentTeam, Long> {

    // getTournamentTeams(): "Registered Teams" on the Tournament Details View, oldest first
    List<TournamentTeam> findByTournamentIdOrderByRegisteredAtAsc(Long tournamentId);

    // a team's registrations ("my tournaments" of a Team Manager), newest first
    List<TournamentTeam> findByTeamIdOrderByRegisteredAtDesc(Long teamId);

    // Rule 2: a team cannot register twice for the same tournament
    boolean existsByTournamentIdAndTeamId(Long tournamentId, Long teamId);

    // Rule 5 (used in Step 9): both teams of a match must be APPROVED in the tournament
    boolean existsByTournamentIdAndTeamIdAndStatus(Long tournamentId, Long teamId, RegistrationStatus status);
}