package com.antonio.sportstournament.registration;

import com.antonio.sportstournament.team.Team;
import com.antonio.sportstournament.tournament.Tournament;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity mapped to the {@code tournament_teams} table (the join table between tournaments and teams).
 * One row is one team's registration in one tournament. The pair (tournament_id, team_id) is unique,
 * so a team cannot register twice in the same tournament.
 */
@Entity
@Table(name = "tournament_teams",
       uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tournament_id","team_id"})
       })
@Getter
@Setter
public class TournamentTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tournament_team_id")
    private Long id;

    // foreign key to tournaments.tournament_id; many registrations per tournament
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id",nullable = false)
    private Tournament tournament;

    // foreign key to teams.team_id; many registrations per team
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id",nullable = false)
    private Team team;

    // PENDING until the organizer approves or rejects it
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private RegistrationStatus status = RegistrationStatus.PENDING;

    // set when the object is created; never changed afterwards
    @Column(name = "registered_at",nullable = false)
    private LocalDateTime registeredAt = LocalDateTime.now();
}
