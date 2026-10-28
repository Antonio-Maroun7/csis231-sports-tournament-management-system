package com.antonio.sportstournamentclient.core;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import javafx.concurrent.Task;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.concurrent.*;

/** Generic static REST client with daemon workers and typed JSON responses. */
public final class ApiClient {
    public static final String BASE_URL = "http://localhost:8081/api";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
    private ApiClient() {}
    public static <T> CompletableFuture<T> get(String path, Class<T> type) { return request("GET", path, null, type); }
    public static <T> CompletableFuture<T> get(String path, TypeReference<T> type) { return request("GET", path, null, type); }
    public static <T> CompletableFuture<T> post(String path, Object body, Class<T> type) { return request("POST", path, body, type); }
    public static <T> CompletableFuture<T> put(String path, Object body, Class<T> type) { return request("PUT", path, body, type); }
    public static CompletableFuture<Void> delete(String path) { return request("DELETE", path, null, Void.class); }
    public static <T> CompletableFuture<T> request(String method, String path, Object body, Class<T> type) {
        return requestInternal(method, path, body, response -> response.body().isBlank() ? null : MAPPER.readValue(response.body(), type));
    }
    public static <T> CompletableFuture<T> request(String method, String path, Object body, TypeReference<T> type) {
        return requestInternal(method, path, body, response -> response.body().isBlank() ? null : MAPPER.readValue(response.body(), type));
    }
    private static <T> CompletableFuture<T> requestInternal(String method, String path, Object body, Parser<T> parser) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var builder = HttpRequest.newBuilder(URI.create(BASE_URL + path)).timeout(TIMEOUT).header("Content-Type","application/json");
                builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)));
                HttpResponse<String> response = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) throw error(response);
                return parser.parse(response);
            } catch (ApiException e) { throw e; } catch (Exception e) {
                String message = e.getMessage();
                if (message == null || message.isBlank()) message = "Cannot reach the server. Is the API running on port 8081?";
                throw new ApiException(0, message);
            }
        }, daemonExecutor());
    }
    private static ApiException error(HttpResponse<String> response) {
        try { JsonNode node = MAPPER.readTree(response.body()); JsonNode detail = node.get("detail"); return new ApiException(response.statusCode(), detail == null ? response.body() : detail.asText()); }
        catch (Exception ignored) { return new ApiException(response.statusCode(), response.body()); }
    }
    public static <T> Task<T> task(String method, String path, Object body, Class<T> type) {
        Task<T> task = new Task<>() { @Override protected T call() throws Exception { return request(method,path,body,type).get(); } };
        Thread thread = new Thread(task, "api-task"); thread.setDaemon(true); thread.start(); return task;
    }
    private static Executor daemonExecutor() { return command -> { Thread thread = new Thread(command, "api-daemon"); thread.setDaemon(true); thread.start(); }; }
    @FunctionalInterface private interface Parser<T> { T parse(HttpResponse<String> response) throws Exception; }
}
