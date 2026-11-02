package com.antonio.sportstournament.exception;

/** Signals that a required verification email could not be delivered; becomes HTTP 503 (Service Unavailable). */
public class EmailDeliveryException extends RuntimeException {
    /**
     * @param message the text shown to the user in the {@code detail} field
     */
    public EmailDeliveryException(String message) {
        super(message);
    }
}
