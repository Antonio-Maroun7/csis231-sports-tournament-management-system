package com.antonio.sportstournamentclient.team;

import com.antonio.sportstournamentclient.core.ApiClient;
import com.antonio.sportstournamentclient.team.dto.TeamRequest;
import com.antonio.sportstournamentclient.team.dto.TeamResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;

/** Team API operations. */
public final class TeamApi {
    private TeamApi() {
    }

    public static List<TeamResponse> all() {
        return ApiClient.get("/teams", new TypeReference<List<TeamResponse>>() {
        });
    }

    public static TeamResponse find(long id) {
        return ApiClient.get("/teams/" + id, TeamResponse.class);
    }

    public static TeamResponse create(TeamRequest request) {
        return ApiClient.post("/teams", request, TeamResponse.class);
    }

    public static TeamResponse update(long id, TeamRequest request) {
        return ApiClient.put("/teams/" + id, request, TeamResponse.class);
    }

    public static void delete(long id) {
        ApiClient.delete("/teams/" + id);
    }
}
