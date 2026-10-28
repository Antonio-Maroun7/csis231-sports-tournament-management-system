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
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Synchronous, typed HTTP client used by the feature APIs. */
public final class ApiClient {
    public static final String BASE_URL = "http://localhost:8081/api";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

    private ApiClient() {
    }

    public static <T> T get(String path, Class<T> type) {
        return request("GET", path, null, type);
    }

    public static <T> T get(String path, TypeReference<T> type) {
        return request("GET", path, null, type);
    }

    public static <T> T post(String path, Object body, Class<T> type) {
        return request("POST", path, body, type);
    }

    public static <T> T post(String path, Object body, TypeReference<T> type) {
        return request("POST", path, body, type);
    }

    public static <T> T put(String path, Object body, Class<T> type) {
        return request("PUT", path, body, type);
    }

    public static <T> T put(String path, Object body, TypeReference<T> type) {
        return request("PUT", path, body, type);
    }

    public static void delete(String path) {
        request("DELETE", path, null, Void.class);
    }

    public static <T> T request(String method, String path, Object body, Class<T> type) {
        return execute(method, path, body, response -> response.body().isBlank()
                ? null : MAPPER.readValue(response.body(), type));
    }

    public static <T> T request(String method, String path, Object body, TypeReference<T> type) {
        return execute(method, path, body, response -> response.body().isBlank()
                ? null : MAPPER.readValue(response.body(), type));
    }

    private static <T> T execute(String method, String path, Object body, Parser<T> parser) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                    .timeout(TIMEOUT)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json");
            builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)));
            HttpResponse<String> response = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw problem(response);
            }
            return parser.parse(response);
        } catch (ApiException exception) {
            throw exception;
        } catch (ConnectException | java.net.http.HttpTimeoutException exception) {
            throw new ApiException(0, "Cannot reach the server. Is the API running on port 8081?");
        } catch (IOException exception) {
            if (exception.getCause() instanceof ConnectException) {
                throw new ApiException(0, "Cannot reach the server. Is the API running on port 8081?");
            }
            throw new IllegalStateException("Request failed", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Request interrupted", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to process API response", exception);
        }
    }

    private static ApiException problem(HttpResponse<String> response) {
        String message = response.body();
        try {
            JsonNode detail = MAPPER.readTree(response.body()).get("detail");
            if (detail != null && !detail.asText().isBlank()) {
                message = detail.asText();
            }
        } catch (Exception ignored) {
            // Preserve the raw response when it is not JSON.
        }
        return new ApiException(response.statusCode(), message);
    }

    @FunctionalInterface
    private interface Parser<T> {
        T parse(HttpResponse<String> response) throws Exception;
    }
}
