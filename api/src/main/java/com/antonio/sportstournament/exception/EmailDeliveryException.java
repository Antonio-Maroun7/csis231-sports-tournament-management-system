package com.antonio.sportstournament.exception;

/** Signals that a required verification email could not be delivered. */
public class EmailDeliveryException extends RuntimeException {
    public EmailDeliveryException(String message) {
        super(message);
    }
}
