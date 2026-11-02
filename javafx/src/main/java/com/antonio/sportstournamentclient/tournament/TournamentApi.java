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

    /**
     * Calls {@code GET /api/tournaments}: every tournament (any logged-in user).
     * Blocking: call it from {@code BackgroundTask}.
     *
     * @return all tournaments
     */
    public static List<TournamentResponse> getAll() {
        return ApiClient.get("/tournaments", new TypeReference<List<TournamentResponse>>() {
        });
    }

    /**
     * Only tournaments with this status, e.g. "OPEN" (open for team registration):
     * {@code GET /api/tournaments?status={status}}. Blocking: call it from {@code BackgroundTask}.
     *
     * @param status {@code DRAFT}, {@code OPEN}, {@code IN_PROGRESS} or {@code COMPLETED}
     * @return the tournaments in that status
     */
    public static List<TournamentResponse> getByStatus(String status) {
        return ApiClient.get("/tournaments?status=" + status, new TypeReference<List<TournamentResponse>>() {
        });
    }

    /**
     * Calls {@code GET /api/tournaments/{id}}. Blocking: call it from {@code BackgroundTask}.
     *
     * @param id tournament id
     * @return the tournament (the API answers 404 if it does not exist)
     */
    public static TournamentResponse getById(Long id) {
        return ApiClient.get("/tournaments/" + id, new TypeReference<TournamentResponse>() {
        });
    }

    /**
     * Calls {@code POST /api/tournaments}: creates a DRAFT tournament owned by the logged-in user
     * (the organizer comes from the JWT). Only ORGANIZER and ADMIN may do it (403 otherwise); 400 if
     * the end date is before the start date. Blocking: call it from {@code BackgroundTask}.
     *
     * @param request name and dates
     * @return the created tournament
     */
    public static TournamentResponse create(TournamentRequest request) {
        return ApiClient.post("/tournaments", request, TournamentResponse.class);
    }

    /**
     * Calls {@code PUT /api/tournaments/{id}}: changes name and dates, not the status (403 unless
     * the caller is its organizer or an ADMIN; 409 if it is completed). Blocking: call it from
     * {@code BackgroundTask}.
     *
     * @param id      tournament id
     * @param request the new name and dates
     * @return the updated tournament
     */
    public static TournamentResponse update(Long id, TournamentRequest request) {
        return ApiClient.put("/tournaments/" + id, request, TournamentResponse.class);
    }

    /**
     * Moves the tournament to its next status (the server only allows one step forward):
     * {@code PUT /api/tournaments/{id}/status}. 403 unless organizer or ADMIN; 409 for an invalid
     * step. Blocking: call it from {@code BackgroundTask}.
     *
     * @param id        tournament id
     * @param newStatus the next status, see {@link TournamentStatuses#next(String)}
     * @return the updated tournament
     */
    public static TournamentResponse changeStatus(Long id, String newStatus) {
        return ApiClient.put("/tournaments/" + id + "/status", new TournamentStatusRequest(newStatus),
                TournamentResponse.class);
    }

    /**
     * Calls {@code DELETE /api/tournaments/{id}} (204 on success; 403 unless organizer or ADMIN;
     * 409 if it already has registrations). Blocking: call it from {@code BackgroundTask}.
     *
     * @param id tournament id
     */
    public static void delete(Long id) {
        ApiClient.delete("/tournaments/" + id);
    }
}
