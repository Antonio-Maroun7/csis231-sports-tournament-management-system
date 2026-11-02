package com.antonio.sportstournament.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 409 CONFLICT - the request is valid but clashes with existing data or a business rule,
 * e.g. an email that is already registered, or a team already registered in the tournament.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException  extends  RuntimeException{
    /**
     * @param message the text shown to the user in the {@code detail} field
     */
    public ConflictException(String message){
        super(message);
    }
}