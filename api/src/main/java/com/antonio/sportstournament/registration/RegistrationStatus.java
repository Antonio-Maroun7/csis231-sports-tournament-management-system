package com.antonio.sportstournament.registration;

/**
 * State of a team's registration in a tournament (column {@code status} of {@code tournament_teams}).
 */
public enum RegistrationStatus {
    /** Waiting for the organizer's decision; every new registration starts here. */
    PENDING,
    /** The organizer accepted the team into the tournament. */
    APPROVED,
    /** The organizer refused the team. */
    REJECTED
}
