package com.antonio.sportstournament.model;

/**
 * Progress of a {@link Match} (SCHEDULED, COMPLETED, CANCELLED), stored in {@code matches.status}.
 * Prepared for the final project; it has no repository, service or API yet.
 */
public enum MatchStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED
}
