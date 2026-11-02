package com.antonio.sportstournamentclient.core;

/**
 * Error returned by the API (or a network problem).
 * status = HTTP status code (400, 401, 404, 409...), or 0 when the server could not be reached.
 * The message is the server's "detail" text, ready to be shown to the user.
 */
public class ApiException extends RuntimeException {
    private final int status;

    /**
     * Creates the exception.
     *
     * @param status  HTTP status code, or 0 when the server could not be reached
     * @param message the server's {@code detail} text (or a network error message)
     */
    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * Returns the HTTP status of the failed call.
     *
     * @return the status code (400, 401, 404, 409...), or 0 for a network or parsing problem
     */
    public int getStatus() {
        return status;
    }
}
