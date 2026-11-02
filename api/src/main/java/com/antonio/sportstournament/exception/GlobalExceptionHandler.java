package com.antonio.sportstournament.exception;


import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Central place that turns exceptions into HTTP errors. Every handler returns a Spring {@code ProblemDetail}
 * JSON body ({@code status}, {@code title}, {@code detail}); the JavaFX client shows the {@code detail} text to the user.
 * Status codes: 400 bad input, 401 not logged in, 403 not allowed, 404 not found, 409 conflict, 503 email failed.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * {@code ResourceNotFoundException} to 404. Example: {@code GET /api/teams/99} when team 99 does not exist.
     *
     * @param ex the exception; its message becomes {@code detail}
     * @return a 404 problem
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex){
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,ex.getMessage());
    }

    /**
     * {@code ConflictException} to 409. Example: registering with an email that already exists.
     *
     * @param ex the exception; its message becomes {@code detail}
     * @return a 409 problem
     */
    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex){
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,ex.getMessage());
    }

    /**
     * {@code UnauthorizedException} to 401. Example: wrong password at login, or a wrong 2FA code.
     *
     * @param ex the exception; its message becomes {@code detail}
     * @return a 401 problem
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException ex){
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,ex.getMessage());
    }

    /**
     * {@code ForbiddenException} to 403. Example: a team manager tries to edit another manager's team.
     *
     * @param ex the exception; its message becomes {@code detail}
     * @return a 403 problem
     */
    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbidden(ForbiddenException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /**
     * Spring Security {@code AccessDeniedException} to 403 with a fixed message.
     * Example: a method-level security check fails inside a controller.
     *
     * @param ex the exception (its message is not shown)
     * @return a 403 problem
     */
    // Normally the security chain answers first; these cover exceptions thrown inside controllers.
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "You do not have permission to do this.");
    }

    /**
     * Spring Security {@code AuthenticationException} to 401 with a fixed "please log in again" message.
     * Example: an authentication failure raised inside a controller.
     *
     * @param ex the exception (its message is not shown)
     * @return a 401 problem
     */
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                "Your session has expired or you are not logged in. Please log in again.");
    }

    /**
     * {@code EmailDeliveryException} to 503. Example: the mail server is down when the 2FA code is sent.
     *
     * @param ex the exception; its message becomes {@code detail}
     * @return a 503 problem
     */
    @ExceptionHandler(EmailDeliveryException.class)
    public ProblemDetail handleEmailDelivery(EmailDeliveryException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    /**
     * Bean Validation failure to 400. The {@code detail} is "field message" of the first bad field, and an extra
     * {@code field} property names it. Example: {@code POST /api/teams} with a blank team name.
     *
     * @param ex the validation exception
     * @return a 400 problem
     */
    // @Valid failed (e.g. blank name, bad email): 400 + which field is wrong
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null
                ? fieldError.getField() + " " + fieldError.getDefaultMessage()
                : "Invalid request";
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        if (fieldError != null) {
            problem.setProperty("field", fieldError.getField());
        }
        return problem;
    }

    /**
     * Unreadable request body to 400. Example: broken JSON, or {@code "role": "BOSS"} which is not a valid enum value.
     *
     * @param ex the exception (its message is not shown)
     * @return a 400 problem
     */
    // Malformed JSON or a wrong enum value (e.g. "role": "BOSS")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request body is missing or malformed");
    }

    /**
     * Database constraint violation to 409. Example: deleting a user who still manages a team.
     *
     * @param ex the exception (its message is not shown, so no database details leak)
     * @return a 409 problem
     */
    // Database UNIQUE / FOREIGN KEY violation (e.g. deleting a user who still manages a team)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleIntegrity(DataIntegrityViolationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Request conflicts with existing data");
    }

    /**
     * {@code BadRequestException} to 400. Example: a business-rule input error that {@code @Valid} cannot detect.
     *
     * @param ex the exception; its message becomes {@code detail}
     * @return a 400 problem
     */
    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleBadRequest(BadRequestException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}