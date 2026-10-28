package com.antonio.sportstournamentclient.auth;
import com.antonio.sportstournamentclient.auth.dto.*; import com.antonio.sportstournamentclient.core.ApiClient; import com.antonio.sportstournamentclient.user.dto.UserResponse; import java.util.concurrent.CompletableFuture;
/** Authentication feature API. */
public final class AuthApi { private AuthApi(){} public static CompletableFuture<LoginResponse> login(LoginRequest r){return ApiClient.post("/auth/login",r,LoginResponse.class);} public static CompletableFuture<UserResponse> register(RegisterRequest r){return ApiClient.post("/auth/register",r,UserResponse.class);} }
