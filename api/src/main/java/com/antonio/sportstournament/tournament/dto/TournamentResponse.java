package com.antonio.sportstournament.tournament.dto;

import com.antonio.sportstournament.tournament.TournamentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JSON returned for one tournament (responses of the {@code /api/tournaments} endpoints).
 *
 * @param id            tournament id
 * @param name          tournament name
 * @param organizerId   id of the user who owns the tournament
 * @param organizerName display name of that organizer
 * @param startDate     first day
 * @param endDate       last day
 * @param status        current lifecycle status
 * @param createdAt     when the tournament was created
 */
public record TournamentResponse(
        Long id,
        String name,
        Long organizerId,
        String organizerName,
        LocalDate startDate,
        LocalDate endDate,
        TournamentStatus status,
        LocalDateTime createdAt
) {
}