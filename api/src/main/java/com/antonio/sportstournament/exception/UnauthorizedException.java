package com.antonio.sportstournament.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 401 UNAUTHORIZED - the caller is not logged in or the credentials are wrong,
 * e.g. a wrong password, or a wrong, used or expired 2FA code.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends  RuntimeException{
    /**
     * @param message the text shown to the user in the {@code detail} field
     */
    public UnauthorizedException (String message){
        super(message);
    }
}