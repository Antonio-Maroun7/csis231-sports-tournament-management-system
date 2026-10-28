package com.antonio.sportstournamentclient.auth;

import com.antonio.sportstournamentclient.auth.dto.LoginRequest;
import com.antonio.sportstournamentclient.auth.dto.LoginResponse;
import com.antonio.sportstournamentclient.auth.dto.RegisterRequest;
import com.antonio.sportstournamentclient.auth.dto.RegisterResponse;
import com.antonio.sportstournamentclient.core.ApiClient;

/**
 * Auth endpoints of the API (/api/auth). Blocking: call from a BackgroundTask.
 */
public class AuthApi {

    public static LoginResponse login(String email, String password) {
        return ApiClient.post("/auth/login", new LoginRequest(email, password), LoginResponse.class);
    }

    public static RegisterResponse register(String name, String email, String password) {
        return ApiClient.post("/auth/register", new RegisterRequest(name, email, password), RegisterResponse.class);
    }
}
