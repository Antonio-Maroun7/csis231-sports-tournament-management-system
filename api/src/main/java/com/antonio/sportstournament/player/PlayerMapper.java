package com.antonio.sportstournament.player;

import com.antonio.sportstournament.player.dto.PlayerRequest;
import com.antonio.sportstournament.player.dto.PlayerResponse;
import com.antonio.sportstournament.team.Team;
import org.springframework.stereotype.Component;

/**
 * Converts between the Player entity and its DTOs.
 */
@Component
public class PlayerMapper {

    public Player toEntity(PlayerRequest request, Team team) {
        Player player = new Player();
        player.setTeam(team);
        updateEntity(player, request);
        return player;
    }

    public void updateEntity(Player player, PlayerRequest request) {
        player.setFirstName(request.firstName().trim());
        player.setLastName(request.lastName().trim());
        player.setJerseyNumber(request.jerseyNumber());
        // blank position -> store NULL (the column is optional)
        player.setPosition(request.position() == null || request.position().isBlank()
                ? null : request.position().trim());
    }

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