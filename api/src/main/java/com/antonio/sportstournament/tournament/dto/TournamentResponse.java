package com.antonio.sportstournament.tournament.dto;

import com.antonio.sportstournament.tournament.TournamentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JSON returned for one tournament.
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