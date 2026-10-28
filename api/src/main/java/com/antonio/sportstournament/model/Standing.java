package com.antonio.sportstournament.model;

import com.antonio.sportstournament.team.Team;
import com.antonio.sportstournament.tournament.Tournament;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "standings",
       uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tournament_id","team_id"})
       })
@Getter
@Setter
public class Standing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "standing_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id",nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id",nullable = false)
    private Team team;

    @Column(nullable = false)
    private int played = 0;

    @Column(nullable = false)
    private int won=0;

    @Column(nullable = false)
    private int drawn=0;

    @Column(nullable = false)
    private int lost=0;

    @Column(nullable = false)
    private int points=0;

    @Column(name = "goal_diff", nullable = false)
    private int goalDiff =0;
}
