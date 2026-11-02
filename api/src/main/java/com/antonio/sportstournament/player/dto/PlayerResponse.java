package com.antonio.sportstournament.player.dto;

/**
 * JSON returned for one player (includes the team name for the Players View "Team" column).
 *
 * @param id           player id
 * @param teamId       id of the player's team
 * @param teamName     name of the player's team
 * @param firstName    first name
 * @param lastName     last name
 * @param jerseyNumber shirt number (0-99)
 * @param position     position, or null if none was given
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