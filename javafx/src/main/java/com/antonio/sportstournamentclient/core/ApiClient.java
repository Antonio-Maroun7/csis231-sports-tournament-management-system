package com.antonio.sportstournamentclient.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Generic HTTP + JSON helpers shared by every feature (based on the instructor's ApiClient).
 *
 * The endpoint methods live in each feature (AuthApi, TeamApi, UserApi) and call these helpers.
 * All methods are BLOCKING: always call them from a background Task, never on the UI thread.
 */
public class ApiClient {
    public static final String BASE_URL = "http://localhost:8081/api";

    private static final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    // -------- Generic helpers --------

    public static <T> T get(String path, TypeReference<T> type) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .GET()
                .build();
        return read(send(request), type);
    }

    public static <T> T post(String path, Object payload, Class<T> type) {
        HttpRequest request = jsonRequest(path)
                .POST(HttpRequest.BodyPublishers.ofString(write(payload)))
                .build();
        return read(send(request), type);
    }

    public static <T> T put(String path, Object payload, Class<T> type) {
        HttpRequest request = jsonRequest(path)
                .PUT(HttpRequest.BodyPublishers.ofString(write(payload)))
                .build();
        return read(send(request), type);
    }

    public static void delete(String path) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .DELETE()
                .build();
        send(request);
    }

    // -------- Internals --------

    private static HttpRequest.Builder jsonRequest(String path) {
        return HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }

    /** Sends the request; returns the body for 2xx, otherwise throws ApiException with the server's message. */
    private static String send(HttpRequest request) {
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                return response.body();
            }
            throw new ApiException(status, extractErrorMessage(response.body(), status));
        } catch (ConnectException | HttpConnectTimeoutException e) {
            throw new ApiException(0, "Cannot reach the server. Is the API running on port 8081?");
        } catch (IOException e) {
            throw new ApiException(0, "Network error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(0, "Request interrupted");
        }
    }

    /** The API returns errors as ProblemDetail JSON: {"status":409,"detail":"..."} - keep the "detail". */
    private static String extractErrorMessage(String body, int status) {
        try {
            JsonNode node = mapper.readTree(body);
            if (node != null && node.hasNonNull("detail")) {
                return node.get("detail").asText();
            }
        } catch (IOException ignored) {
            // not JSON: fall through to the generic message
        }
        return "Server error (status " + status + ")";
    }

    private static String write(Object payload) {
        try {
            return mapper.writeValueAsString(payload);
        } catch (IOException e) {
            throw new ApiException(0, "Could not create request: " + e.getMessage());
        }
    }

    private static <T> T read(String body, TypeReference<T> type) {
        try {
            return mapper.readValue(body, type);
        } catch (IOException e) {
            throw new ApiException(0, "Could not read server response: " + e.getMessage());
        }
    }

    private static <T> T read(String body, Class<T> type) {
        try {
            return mapper.readValue(body, type);
        } catch (IOException e) {
            throw new ApiException(0, "Could not read server response: " + e.getMessage());
        }
    }
}
