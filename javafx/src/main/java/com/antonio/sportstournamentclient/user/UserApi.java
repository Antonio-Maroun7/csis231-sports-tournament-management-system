package com.antonio.sportstournamentclient.user;
import com.antonio.sportstournamentclient.core.ApiClient; import com.antonio.sportstournamentclient.user.dto.*; import java.util.concurrent.CompletableFuture;
/** User administration API. */
public final class UserApi {private UserApi(){} public static CompletableFuture<UserResponse[]> all(){return ApiClient.get("/users",new com.fasterxml.jackson.core.type.TypeReference<UserResponse[]>(){});} public static CompletableFuture<UserResponse> create(UserCreateRequest r){return ApiClient.post("/users",r,UserResponse.class);} public static CompletableFuture<Void> delete(long id){return ApiClient.delete("/users/"+id);}}

