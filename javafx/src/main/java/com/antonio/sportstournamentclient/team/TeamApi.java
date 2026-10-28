package com.antonio.sportstournamentclient.team;

import com.antonio.sportstournamentclient.core.ApiClient;
import com.antonio.sportstournamentclient.team.dto.TeamRequest;
import com.antonio.sportstournamentclient.team.dto.TeamResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * Team endpoints of the API (/api/teams). Blocking: call from a BackgroundTask.
 */
public class TeamApi {

    public static List<TeamResponse> getAll() {
        return ApiClient.get("/teams", new TypeReference<List<TeamResponse>>() {
        });
    }

    /** Teams managed by one Team Manager ("my teams"). */
    public static List<TeamResponse> getByManager(Long managerId) {
        return ApiClient.get("/teams?managerId=" + managerId, new TypeReference<List<TeamResponse>>() {
        });
    }

    public static TeamResponse getById(Long id) {
        return ApiClient.get("/teams/" + id, new TypeReference<TeamResponse>() {
        });
    }

    public static TeamResponse create(TeamRequest request) {
        return ApiClient.post("/teams", request, TeamResponse.class);
    }

    public static TeamResponse update(Long id, TeamRequest request) {
        return ApiClient.put("/teams/" + id, request, TeamResponse.class);
    }

    public static void delete(Long id) {
        ApiClient.delete("/teams/" + id);
    }
}
