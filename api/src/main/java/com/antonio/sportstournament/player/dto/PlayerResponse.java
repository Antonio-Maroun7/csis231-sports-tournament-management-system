package com.antonio.sportstournament.player.dto;

public record PlayerResponse(
        Long id,
        Long teamId,
        String firstName,
        String lastName,
        Integer jerseyNumber,
        String position
) {
}
