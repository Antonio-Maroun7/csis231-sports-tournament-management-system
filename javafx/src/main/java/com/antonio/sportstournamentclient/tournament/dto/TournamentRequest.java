package com.antonio.sportstournamentclient.tournament.dto;

import java.time.LocalDate;

/**
 * Body of POST /api/tournaments and PUT /api/tournaments/{id}.
 * The organizer is the logged-in user (taken from the JWT by the server).
 */
public record TournamentRequest(String name, LocalDate startDate, LocalDate endDate) {
}
