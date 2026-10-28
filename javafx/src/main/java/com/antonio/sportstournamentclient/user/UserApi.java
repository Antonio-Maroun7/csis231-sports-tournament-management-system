package com.antonio.sportstournamentclient.user;

import com.antonio.sportstournamentclient.core.ApiClient;
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

    public static void delete(Long id) {
        ApiClient.delete("/users/" + id);
    }
}
