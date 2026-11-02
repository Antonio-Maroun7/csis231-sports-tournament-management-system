package com.antonio.sportstournamentclient.auth;

import com.antonio.sportstournamentclient.auth.dto.AuthResult;
import com.antonio.sportstournamentclient.auth.dto.LoginRequest;
import com.antonio.sportstournamentclient.auth.dto.LoginResponse;
import com.antonio.sportstournamentclient.auth.dto.RegisterRequest;
import com.antonio.sportstournamentclient.auth.dto.RegisterResponse;
import com.antonio.sportstournamentclient.auth.dto.ResendCodeRequest;
import com.antonio.sportstournamentclient.auth.dto.VerifyTwoFactorRequest;
import com.antonio.sportstournamentclient.core.ApiClient;

/**
 * Auth endpoints of the API (/api/auth). Blocking: call from a BackgroundTask.
 */
public class AuthApi {

    /** Step 1: email + password. Either logged in, or a 6-digit code was emailed (2FA). */
    public static AuthResult login(String email, String password) {
        return ApiClient.post("/auth/login", new LoginRequest(email, password), AuthResult.class);
    }

    /** Step 2 (2FA only): the code from the email. */
    public static LoginResponse verifyTwoFactor(String challengeToken, String code) {
        return ApiClient.post("/auth/verify-2fa", new VerifyTwoFactorRequest(challengeToken, code), LoginResponse.class);
    }

    /** Emails a new code; the answer carries a NEW challengeToken and expiry. */
    public static AuthResult resendCode(String challengeToken) {
        return ApiClient.post("/auth/resend-2fa", new ResendCodeRequest(challengeToken), AuthResult.class);
    }

    public static RegisterResponse register(String name, String email, String password) {
        return ApiClient.post("/auth/register", new RegisterRequest(name, email, password), RegisterResponse.class);
    }
}
