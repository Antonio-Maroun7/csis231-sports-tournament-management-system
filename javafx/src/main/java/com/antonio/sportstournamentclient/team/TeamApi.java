package com.antonio.sportstournamentclient.team;
import com.antonio.sportstournamentclient.core.ApiClient; import com.antonio.sportstournamentclient.team.dto.*; import java.util.concurrent.CompletableFuture;
/** Team feature API. */
public final class TeamApi {private TeamApi(){} public static CompletableFuture<TeamResponse[]> all(){return ApiClient.get("/teams",new com.fasterxml.jackson.core.type.TypeReference<TeamResponse[]>(){});} public static CompletableFuture<TeamResponse> create(TeamRequest r){return ApiClient.post("/teams",r,TeamResponse.class);} public static CompletableFuture<TeamResponse> update(long id,TeamRequest r){return ApiClient.put("/teams/"+id,r,TeamResponse.class);} public static CompletableFuture<Void> delete(long id){return ApiClient.delete("/teams/"+id);} public static CompletableFuture<TeamResponse> find(long id){return ApiClient.get("/teams/"+id,TeamResponse.class);}}

