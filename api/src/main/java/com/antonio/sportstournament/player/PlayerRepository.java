package com.antonio.sportstournament.player;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Data access for the players table. Spring Data generates the SQL from the method names.
 */
public interface PlayerRepository extends JpaRepository<Player, Long> {

    // getPlayersByTeam(): SELECT * FROM players WHERE team_id = ? ORDER BY jersey_number
    List<Player> findByTeamIdOrderByJerseyNumberAsc(Long teamId);

    // Business rule: jersey number unique inside a team (used when ADDING a player)
    boolean existsByTeamIdAndJerseyNumber(Long teamId, Integer jerseyNumber);

    // Same rule when EDITING: ignore the player being edited
    boolean existsByTeamIdAndJerseyNumberAndIdNot(Long teamId, Integer jerseyNumber, Long id);
}