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

    /**
     * Calls {@code GET /api/teams}: every team (any logged-in user).
     * Blocking: call it from {@code BackgroundTask}.
     *
     * @return all teams, ordered by the server
     */
    public static List<TeamResponse> getAll() {
        return ApiClient.get("/teams", new TypeReference<List<TeamResponse>>() {
        });
    }

    /**
     * Teams managed by one Team Manager ("my teams"): {@code GET /api/teams?managerId={managerId}}.
     * Blocking: call it from {@code BackgroundTask}.
     *
     * @param managerId id of the Team Manager
     * @return that manager's teams (the API answers 404 if the user does not exist)
     */
    public static List<TeamResponse> getByManager(Long managerId) {
        return ApiClient.get("/teams?managerId=" + managerId, new TypeReference<List<TeamResponse>>() {
        });
    }

    /**
     * Calls {@code GET /api/teams/{id}}. Blocking: call it from {@code BackgroundTask}.
     *
     * @param id team id
     * @return the team (the API answers 404 if it does not exist)
     */
    public static TeamResponse getById(Long id) {
        return ApiClient.get("/teams/" + id, new TypeReference<TeamResponse>() {
        });
    }

    /**
     * Calls {@code POST /api/teams}: creates a team managed by the logged-in user (the manager
     * comes from the JWT, not from the request). Only TEAM_MANAGER and ADMIN may do it (403 otherwise).
     * Blocking: call it from {@code BackgroundTask}.
     *
     * @param request the team name
     * @return the created team
     */
    public static TeamResponse create(TeamRequest request) {
        return ApiClient.post("/teams", request, TeamResponse.class);
    }

    /**
     * Calls {@code PUT /api/teams/{id}}: renames a team (403 unless the caller is its manager or an ADMIN).
     * Blocking: call it from {@code BackgroundTask}.
     *
     * @param id      team id
     * @param request the new team name
     * @return the updated team
     */
    public static TeamResponse update(Long id, TeamRequest request) {
        return ApiClient.put("/teams/" + id, request, TeamResponse.class);
    }

    /**
     * Calls {@code DELETE /api/teams/{id}} (204 on success; 403 unless manager or ADMIN; 409 if the
     * team still has players or registrations). Blocking: call it from {@code BackgroundTask}.
     *
     * @param id team id
     */
    public static void delete(Long id) {
        ApiClient.delete("/teams/" + id);
    }
}
