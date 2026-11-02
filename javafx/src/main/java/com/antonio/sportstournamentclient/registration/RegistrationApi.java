package com.antonio.sportstournamentclient.registration;

import com.antonio.sportstournamentclient.core.ApiClient;
import com.antonio.sportstournamentclient.registration.dto.RegistrationRequest;
import com.antonio.sportstournamentclient.registration.dto.RegistrationResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * Registration endpoints of the API (tournament_teams). Blocking: call from a BackgroundTask.
 */
public class RegistrationApi {

    /**
     * Calls {@code GET /api/tournaments/{tournamentId}/registrations}: the teams registered in a
     * tournament (any logged-in user). Blocking: call it from {@code BackgroundTask}.
     *
     * @param tournamentId tournament id
     * @return its registrations (404 if the tournament does not exist)
     */
    public static List<RegistrationResponse> getByTournament(Long tournamentId) {
        return ApiClient.get("/tournaments/" + tournamentId + "/registrations",
                new TypeReference<List<RegistrationResponse>>() {
                });
    }

    /**
     * Calls {@code GET /api/teams/{teamId}/registrations}: the tournaments one team registered for.
     * Blocking: call it from {@code BackgroundTask}.
     *
     * @param teamId team id
     * @return that team's registrations (404 if the team does not exist)
     */
    public static List<RegistrationResponse> getByTeam(Long teamId) {
        return ApiClient.get("/teams/" + teamId + "/registrations",
                new TypeReference<List<RegistrationResponse>>() {
                });
    }

    /**
     * Calls {@code POST /api/tournaments/{tournamentId}/registrations}: registers a team; the new
     * registration is PENDING. Only the team's own manager or an ADMIN (403 otherwise); 409 if the
     * tournament is not OPEN or the team is already registered. Blocking: call it from {@code BackgroundTask}.
     *
     * @param tournamentId tournament to join
     * @param teamId       team to register
     * @return the created registration
     */
    public static RegistrationResponse register(Long tournamentId, Long teamId) {
        return ApiClient.post("/tournaments/" + tournamentId + "/registrations",
                new RegistrationRequest(teamId), RegistrationResponse.class);
    }

    /**
     * Calls {@code PUT /api/registrations/{id}/approve}: ORGANIZER or ADMIN only, and the caller
     * must own the tournament (403 otherwise); 409 unless the registration is PENDING.
     * Blocking: call it from {@code BackgroundTask}.
     *
     * @param registrationId registration id
     * @return the registration, now APPROVED
     */
    public static RegistrationResponse approve(Long registrationId) {
        return ApiClient.put("/registrations/" + registrationId + "/approve", RegistrationResponse.class);
    }

    /**
     * Calls {@code PUT /api/registrations/{id}/reject}: same rules as {@link #approve(Long)}.
     * Blocking: call it from {@code BackgroundTask}.
     *
     * @param registrationId registration id
     * @return the registration, now REJECTED
     */
    public static RegistrationResponse reject(Long registrationId) {
        return ApiClient.put("/registrations/" + registrationId + "/reject", RegistrationResponse.class);
    }

    /**
     * Calls {@code DELETE /api/registrations/{id}}: the team's manager (or an ADMIN) withdraws the
     * team (204 on success; 409 if the tournament is no longer OPEN). Blocking: call it from
     * {@code BackgroundTask}.
     *
     * @param registrationId registration id
     */
    public static void withdraw(Long registrationId) {
        ApiClient.delete("/registrations/" + registrationId);
    }
}
