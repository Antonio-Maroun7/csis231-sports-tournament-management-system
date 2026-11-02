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

    /**
     * Calls {@code GET /api/users}: every account (ADMIN only, 403 otherwise).
     * Blocking: call it from {@code BackgroundTask}.
     *
     * @return all users
     */
    public static List<UserResponse> getAll() {
        return ApiClient.get("/users", new TypeReference<List<UserResponse>>() {
        });
    }

    /**
     * Calls {@code GET /api/users/{id}} (ADMIN only). Blocking: call it from {@code BackgroundTask}.
     *
     * @param id user id
     * @return the user (the API answers 404 if it does not exist)
     */
    public static UserResponse getById(Long id) {
        return ApiClient.get("/users/" + id, new TypeReference<UserResponse>() {
        });
    }

    /**
     * Calls {@code POST /api/users}: an administrator creates an account with any role (ADMIN only;
     * 409 if the email is already used). Blocking: call it from {@code BackgroundTask}.
     *
     * @param request name, email, password and role of the new account
     * @return the created user
     */
    public static UserResponse create(UserCreateRequest request) {
        return ApiClient.post("/users", request, UserResponse.class);
    }

    /**
     * Turn email 2FA on/off with {@code PUT /api/users/{id}/two-factor}. A user can change only their
     * own setting (403), and the server refuses to turn it off for Admins and Organizers (409).
     * Blocking: call it from {@code BackgroundTask}.
     *
     * @param id      id of the logged-in user
     * @param enabled {@code true} to turn 2FA on
     * @return the updated user
     */
    public static UserResponse updateTwoFactor(Long id, boolean enabled) {
        return ApiClient.put("/users/" + id + "/two-factor", new TwoFactorSettingRequest(enabled), UserResponse.class);
    }

    /**
     * Calls {@code DELETE /api/users/{id}} (ADMIN only; 204 on success; 409 if the admin deletes their
     * own account or the user is still referenced, e.g. manages a team). Blocking: call it from
     * {@code BackgroundTask}.
     *
     * @param id user id
     */
    public static void delete(Long id) {
        ApiClient.delete("/users/" + id);
    }
}
