package com.antonio.sportstournament.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 404 NOT FOUND - thrown when a record with the requested id does not exist,
 * e.g. {@code GET /api/teams/99} when there is no team 99.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    /**
     * @param message the text shown to the user in the {@code detail} field
     */
    public ResourceNotFoundException(String message){
        super(message);
    }
}