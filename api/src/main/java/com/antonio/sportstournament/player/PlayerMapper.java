package com.antonio.sportstournament.player;

import com.antonio.sportstournament.player.dto.PlayerRequest;
import com.antonio.sportstournament.player.dto.PlayerResponse;
import com.antonio.sportstournament.team.Team;
import org.springframework.stereotype.Component;

/**
 * Converts between the Player entity and its DTOs.
 * {@link PlayerRequest} to {@link Player} on input, {@link Player} to {@link PlayerResponse} on output.
 */
@Component
public class PlayerMapper {

    /**
     * Builds a new Player for a team from a create request.
     *
     * @param request the validated request
     * @param team    the team taken from the URL
     * @return an unsaved Player
     */
    public Player toEntity(PlayerRequest request, Team team) {
        Player player = new Player();
        player.setTeam(team);
        updateEntity(player, request);
        return player;
    }

    /**
     * Copies the request fields onto a player (names and position are trimmed). A blank position becomes NULL.
     * The team is not changed.
     *
     * @param player  the entity to update
     * @param request the validated request
     */
    public void updateEntity(Player player, PlayerRequest request) {
        player.setFirstName(request.firstName().trim());
        player.setLastName(request.lastName().trim());
        player.setJerseyNumber(request.jerseyNumber());
        // blank position -> store NULL (the column is optional)
        player.setPosition(request.position() == null || request.position().isBlank()
                ? null : request.position().trim());
    }

    /**
     * Converts a player to the JSON sent to clients; adds the team id and name from the player's team.
     *
     * @param player the entity (its team must be loadable)
     * @return the response DTO
     */
    public PlayerResponse toResponse(Player player) {
        Team team = player.getTeam();
        return new PlayerResponse(
                player.getId(),
                team.getId(),
                team.getTeamName(),
                player.getFirstName(),
                player.getLastName(),
                player.getJerseyNumber(),
                player.getPosition()
        );
    }
}