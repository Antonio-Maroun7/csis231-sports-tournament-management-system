package com.antonio.sportstournamentclient.tournament;

import com.antonio.sportstournamentclient.core.ApiClient;
import com.antonio.sportstournamentclient.tournament.dto.TournamentRequest;
import com.antonio.sportstournamentclient.tournament.dto.TournamentResponse;
import com.antonio.sportstournamentclient.tournament.dto.TournamentStatusRequest;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * Tournament endpoints of the API (/api/tournaments). Blocking: call from a BackgroundTask.
 */
public class TournamentApi {

    public static List<TournamentResponse> getAll() {
        return ApiClient.get("/tournaments", new TypeReference<List<TournamentResponse>>() {
        });
    }

    /** Only tournaments with this status, e.g. "OPEN" (open for team registration). */
    public static List<TournamentResponse> getByStatus(String status) {
        return ApiClient.get("/tournaments?status=" + status, new TypeReference<List<TournamentResponse>>() {
        });
    }

    public static TournamentResponse getById(Long id) {
        return ApiClient.get("/tournaments/" + id, new TypeReference<TournamentResponse>() {
        });
    }

    public static TournamentResponse create(TournamentRequest request) {
        return ApiClient.post("/tournaments", request, TournamentResponse.class);
    }

    public static TournamentResponse update(Long id, TournamentRequest request) {
        return ApiClient.put("/tournaments/" + id, request, TournamentResponse.class);
    }

    /** Moves the tournament to its next status (the server only allows one step forward). */
    public static TournamentResponse changeStatus(Long id, String newStatus) {
        return ApiClient.put("/tournaments/" + id + "/status", new TournamentStatusRequest(newStatus),
                TournamentResponse.class);
    }

    public static void delete(Long id) {
        ApiClient.delete("/tournaments/" + id);
    }
}
