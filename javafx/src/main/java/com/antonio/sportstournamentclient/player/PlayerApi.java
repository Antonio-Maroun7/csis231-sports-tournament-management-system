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

    /**
     * Calls {@code GET /api/teams/{teamId}/players}: the roster of one team. Blocking: call it from
     * {@code BackgroundTask}.
     *
     * @param teamId team id
     * @return the players of that team (404 if the team does not exist)
     */
    public static List<PlayerResponse> getByTeam(Long teamId) {
        return ApiClient.get("/teams/" + teamId + "/players", new TypeReference<List<PlayerResponse>>() {
        });
    }

    /**
     * Calls {@code POST /api/teams/{teamId}/players}: adds a player to a team (403 unless the caller
     * manages the team or is an ADMIN; 409 if the jersey number is already used in that team).
     * Blocking: call it from {@code BackgroundTask}.
     *
     * @param teamId  team id
     * @param request the player data
     * @return the created player
     */
    public static PlayerResponse add(Long teamId, PlayerRequest request) {
        return ApiClient.post("/teams/" + teamId + "/players", request, PlayerResponse.class);
    }

    /**
     * Calls {@code PUT /api/players/{id}}: edits a player (403 unless manager or ADMIN; 409 if the
     * new jersey number is taken in the team). Blocking: call it from {@code BackgroundTask}.
     *
     * @param id      player id
     * @param request the new player data
     * @return the updated player
     */
    public static PlayerResponse update(Long id, PlayerRequest request) {
        return ApiClient.put("/players/" + id, request, PlayerResponse.class);
    }

    /**
     * Calls {@code DELETE /api/players/{id}} (204 on success; 403 unless manager or ADMIN).
     * Blocking: call it from {@code BackgroundTask}.
     *
     * @param id player id
     */
    public static void remove(Long id) {
        ApiClient.delete("/players/" + id);
    }
}
