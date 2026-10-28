package com.antonio.sportstournamentclient.auth;

import com.antonio.sportstournamentclient.auth.dto.LoginRequest;
import com.antonio.sportstournamentclient.auth.dto.LoginResponse;
import com.antonio.sportstournamentclient.auth.dto.RegisterRequest;
import com.antonio.sportstournamentclient.core.ApiClient;
import com.antonio.sportstournamentclient.user.dto.UserResponse;

/** Authentication API operations. */
public final class AuthApi {
    private AuthApi() {
    }

    public static LoginResponse login(LoginRequest request) {
        return ApiClient.post("/auth/login", request, LoginResponse.class);
    }

    public static UserResponse register(RegisterRequest request) {
        return ApiClient.post("/auth/register", request, UserResponse.class);
    }
}
