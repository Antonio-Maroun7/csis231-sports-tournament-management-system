package com.antonio.sportstournament.player.dto;

/**
 * JSON returned for one player (includes the team name for the Players View "Team" column).
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
}