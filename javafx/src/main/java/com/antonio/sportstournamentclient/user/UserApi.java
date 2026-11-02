package com.antonio.sportstournamentclient.user;

import com.antonio.sportstournamentclient.core.ApiClient;
import com.antonio.sportstournamentclient.user.dto.TwoFactorSettingRequest;
import com.antonio.sportstournamentclient.user.dto.UserCreateRequest;
import com.antonio.sportstournamentclient.user.dto.UserResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * User endpoints of the API (/api/users). Blocking: call from a BackgroundTask.
 */
public class UserApi {

    public static List<UserResponse> getAll() {
        return ApiClient.get("/users", new TypeReference<List<UserResponse>>() {
        });
    }

    public static UserResponse getById(Long id) {
        return ApiClient.get("/users/" + id, new TypeReference<UserResponse>() {
        });
    }

    public static UserResponse create(UserCreateRequest request) {
        return ApiClient.post("/users", request, UserResponse.class);
    }

    /** Turn email 2FA on/off. The server refuses to turn it off for Admins and Organizers (409). */
    public static UserResponse updateTwoFactor(Long id, boolean enabled) {
        return ApiClient.put("/users/" + id + "/two-factor", new TwoFactorSettingRequest(enabled), UserResponse.class);
    }

    public static void delete(Long id) {
        ApiClient.delete("/users/" + id);
    }
}
