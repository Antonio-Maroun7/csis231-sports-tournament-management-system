package com.antonio.sportstournamentclient.user;

import com.antonio.sportstournamentclient.core.ApiClient;
import com.antonio.sportstournamentclient.user.dto.UserCreateRequest;
import com.antonio.sportstournamentclient.user.dto.UserResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;

/** User administration API operations. */
public final class UserApi {
    private UserApi() {
    }

    public static List<UserResponse> all() {
        return ApiClient.get("/users", new TypeReference<List<UserResponse>>() {
        });
    }

    public static UserResponse create(UserCreateRequest request) {
        return ApiClient.post("/users", request, UserResponse.class);
    }

    public static UserResponse update(long id, UserCreateRequest request) {
        return ApiClient.put("/users/" + id, request, UserResponse.class);
    }

    public static void delete(long id) {
        ApiClient.delete("/users/" + id);
    }
}
