package com.antonio.sportstournament.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 400 BAD REQUEST - invalid input that Bean Validation (@Valid) cannot detect,
 * e.g. "user 2 is not a Team Manager".
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}