package com.antonio.sportstournament.team;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Data access for the teams table. Spring Data generates the SQL from the method names.
 */
public interface TeamRepository extends JpaRepository<Team, Long> {

    // SELECT * FROM teams ORDER BY team_name
    List<Team> findAllByOrderByTeamNameAsc();

    // SELECT * FROM teams WHERE manager_id = ? ORDER BY team_name   ("my teams")
    List<Team> findByManagerIdOrderByTeamNameAsc(Long managerId);
}