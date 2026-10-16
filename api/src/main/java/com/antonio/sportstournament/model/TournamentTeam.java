package com.antonio.sportstournament.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id",nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id",nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private RegistrationStatus status = RegistrationStatus.PENDING;

    @Column(name = "registered_at",nullable = false)
    private LocalDateTime registredAt = LocalDateTime.now();
}
