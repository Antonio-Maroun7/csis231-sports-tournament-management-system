package com.antonio.sportstournament.tournament;

import com.antonio.sportstournament.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity mapped to the {@code tournaments} table. One row is one tournament created by an organizer.
 * Many tournaments belong to one organizer ({@link User}); many teams can register in one tournament
 * (see {@code TournamentTeam}).
 */
@Entity
@Table(name = "tournaments")
@Getter
@Setter
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tournament_id")
    private Long id;

    // required, max 120 characters
    @Column(nullable = false,length = 120)
    private String name;

    // foreign key to users.user_id: the ORGANIZER (or ADMIN) who owns the tournament
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id",nullable = false)
    private User organizer;

    @Column(name = "start_date",nullable = false)
    private LocalDate startDate;

    // never before startDate (checked by TournamentService)
    @Column(name = "end_date",nullable = false)
    private LocalDate endDate;

    // lifecycle stage, stored as text; a new tournament starts as DRAFT
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private TournamentStatus status = TournamentStatus.DRAFT;

    // set when the object is created; never changed afterwards
    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
