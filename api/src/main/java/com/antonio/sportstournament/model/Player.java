package com.antonio.sportstournament.model;

import com.antonio.sportstournament.team.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id",nullable = false)
    private Team team;

    @Column(name = "first_name",nullable = false,length = 50)
    private String firstName;

    @Column(name = "last_name",nullable = false,length = 50)
    private String lastName;

    @Column(name = "jersey_number",nullable = false)
    private Integer jerseyNumber;

    @Column(length = 30)
    private String position;
}
