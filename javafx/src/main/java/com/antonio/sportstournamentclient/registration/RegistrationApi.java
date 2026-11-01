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

    /** Teams registered in a tournament. */
    public static List<RegistrationResponse> getByTournament(Long tournamentId) {
        return ApiClient.get("/tournaments/" + tournamentId + "/registrations",
                new TypeReference<List<RegistrationResponse>>() {
                });
    }

    /** Tournaments a team registered for. */
    public static List<RegistrationResponse> getByTeam(Long teamId) {
        return ApiClient.get("/teams/" + teamId + "/registrations",
                new TypeReference<List<RegistrationResponse>>() {
                });
    }

    public static RegistrationResponse register(Long tournamentId, Long teamId) {
        return ApiClient.post("/tournaments/" + tournamentId + "/registrations",
                new RegistrationRequest(teamId), RegistrationResponse.class);
    }

    public static RegistrationResponse approve(Long registrationId) {
        return ApiClient.put("/registrations/" + registrationId + "/approve", RegistrationResponse.class);
    }

    public static RegistrationResponse reject(Long registrationId) {
        return ApiClient.put("/registrations/" + registrationId + "/reject", RegistrationResponse.class);
    }

    public static void withdraw(Long registrationId) {
        ApiClient.delete("/registrations/" + registrationId);
    }
}
