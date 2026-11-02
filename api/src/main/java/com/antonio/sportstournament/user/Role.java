package com.antonio.sportstournament.user;

/**
 * The role of a user. It is stored as text in {@code users.role} and decides which API
 * endpoints the user may call (URL rules in {@code SecurityConfig}, ownership rules in the services).
 */
public enum Role {
    /** Full access: manages users and everything else. Must log in with the emailed 2FA code. */
    ADMIN,
    /** Creates and manages tournaments and approves or rejects team registrations. Must use the emailed 2FA code. */
    ORGANIZER,
    /** Owns teams and players and registers teams in tournaments. This is the default role. */
    TEAM_MANAGER
}
