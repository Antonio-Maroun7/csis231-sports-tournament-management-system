package com.antonio.sportstournament.team;

import com.antonio.sportstournament.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity mapped to the {@code teams} table. One row is one team.
 * Many players belong to one team, and one team is owned by exactly one manager (a {@link User}).
 * A team can be registered in many tournaments (see {@code TournamentTeam}).
 */
@Entity
@Table(name = "teams")
@Getter
@Setter
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long id;

    // required, at most 100 characters
    @Column(name = "team_name",nullable = false,length = 100)
    private String teamName;

    // foreign key to users.user_id: the TEAM_MANAGER (or ADMIN) who created the team; many teams per manager
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="manager_id",nullable = false)
    private User manager;

    // set when the object is created; never changed afterwards
    @Column(name="created_at",nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
