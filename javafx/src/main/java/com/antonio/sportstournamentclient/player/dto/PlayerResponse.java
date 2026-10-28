package com.antonio.sportstournamentclient.player.dto;

/**
 * One player as returned by the players endpoints.
 */
public record PlayerResponse(
        Long id,
        Long teamId,
        String teamName,
        String firstName,
        String lastName,
        Integer jerseyNumber,
        String position
) {
    /** "Karim Haddad" */
    public String fullName() {
        return firstName + " " + lastName;
    }
}
