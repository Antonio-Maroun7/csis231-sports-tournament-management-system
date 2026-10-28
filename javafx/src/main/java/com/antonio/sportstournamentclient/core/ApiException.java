package com.antonio.sportstournamentclient.core;

/** Describes an HTTP failure returned by the tournament API. */
public class ApiException extends RuntimeException {
    private final int status;
    public ApiException(int status, String message) {
        super(status == 0 ? "Cannot reach the server. Is the API running on port 8081?" : message);
        this.status = status;
    }
    public int status() { return status; }
}
