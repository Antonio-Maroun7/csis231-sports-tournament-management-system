package com.antonio.sportstournamentclient.tournament.dto;

import java.time.LocalDate;

/**
 * Body of POST /api/tournaments and PUT /api/tournaments/{id}.
 * organizerId = the Organizer who owns the tournament (until JWT login, the client sends it).
 */
public record TournamentRequest(String name, LocalDate startDate, LocalDate endDate, Long organizerId) {
}
