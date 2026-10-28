package com.antonio.sportstournamentclient.player;

import com.antonio.sportstournamentclient.core.ApiClient;
import com.antonio.sportstournamentclient.player.dto.PlayerRequest;
import com.antonio.sportstournamentclient.player.dto.PlayerResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * Player endpoints of the API. Blocking: call from a BackgroundTask.
 *   GET    /api/teams/{teamId}/players
 *   POST   /api/teams/{teamId}/players
 *   PUT    /api/players/{id}
 *   DELETE /api/players/{id}
 */
public class PlayerApi {

    public static List<PlayerResponse> getByTeam(Long teamId) {
        return ApiClient.get("/teams/" + teamId + "/players", new TypeReference<List<PlayerResponse>>() {
        });
    }

    public static PlayerResponse add(Long teamId, PlayerRequest request) {
        return ApiClient.post("/teams/" + teamId + "/players", request, PlayerResponse.class);
    }

    public static PlayerResponse update(Long id, PlayerRequest request) {
        return ApiClient.put("/players/" + id, request, PlayerResponse.class);
    }

    public static void remove(Long id) {
        ApiClient.delete("/players/" + id);
    }
}
