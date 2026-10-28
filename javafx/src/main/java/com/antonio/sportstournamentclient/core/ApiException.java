package com.antonio.sportstournamentclient.core;

/**
 * Error returned by the API (or a network problem).
 * status = HTTP status code (400, 401, 404, 409...), or 0 when the server could not be reached.
 * The message is the server's "detail" text, ready to be shown to the user.
 */
public class ApiException extends RuntimeException {
    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
