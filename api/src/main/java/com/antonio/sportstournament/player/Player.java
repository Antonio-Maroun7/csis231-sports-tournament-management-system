package com.antonio.sportstournament.player;

import com.antonio.sportstournament.team.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity mapped to the {@code players} table. One row is one player on one team's roster.
 * Many players belong to one {@link Team}. The pair (team_id, jersey_number) is unique,
 * so two players of the same team cannot wear the same number.
 */
@Entity
@Table(name = "players",
       uniqueConstraints = {
        @UniqueConstraint(columnNames = {"team_id","jersey_number"})
       })
@Getter
@Setter
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "player_id")
    private Long id;

    // foreign key to teams.team_id: the roster this player belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id",nullable = false)
    private Team team;

    @Column(name = "first_name",nullable = false,length = 50)
    private String firstName;

    @Column(name = "last_name",nullable = false,length = 50)
    private String lastName;

    // shirt number 0-99 (checked in PlayerRequest); unique inside one team
    @Column(name = "jersey_number",nullable = false)
    private Integer jerseyNumber;

    // optional free text such as Goalkeeper or Forward; NULL when not given
    @Column(length = 30)
    private String position;
}
