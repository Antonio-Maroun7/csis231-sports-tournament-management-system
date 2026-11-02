package com.antonio.sportstournament.tournament;

/**
 * Lifecycle of a tournament. It only moves forward, one step at a time
 * (DRAFT, OPEN, IN_PROGRESS, COMPLETED) through {@code PUT /api/tournaments/{id}/status}.
 */
public enum TournamentStatus {
    /** Just created; the organizer is still preparing it. Teams cannot register yet. */
    DRAFT,
    /** Open for registration: teams can register and the organizer approves or rejects them. */
    OPEN,
    /** Registration is closed and the tournament is being played. */
    IN_PROGRESS,
    /** Finished; the tournament can no longer be edited. */
    COMPLETED
}
