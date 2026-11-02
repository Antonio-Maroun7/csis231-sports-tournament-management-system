package com.antonio.sportstournament.model;

/**
 * Approval state of a {@link Venue} (PENDING, APPROVED, DISABLED, REJECTED), stored in {@code venues.status}.
 * Prepared for the final project; it has no repository, service or API yet.
 */
public enum VenueStatus {
    PENDING,
    APPROVED,
    DISABLED,
    REJECTED
}
