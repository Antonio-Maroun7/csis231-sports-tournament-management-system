package com.antonio.sportstournamentclient.tournament.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One tournament as returned by /api/tournaments.
 * status = DRAFT | OPEN | IN_PROGRESS | COMPLETED
 */
public record TournamentResponse(
        Long id,
        String name,
        Long organizerId,
        String organizerName,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        LocalDateTime createdAt
) {
}
