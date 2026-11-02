package com.antonio.sportstournament.auth;

import com.antonio.sportstournament.auth.dto.LoginRequest;
import com.antonio.sportstournament.auth.dto.LoginResponse;
import com.antonio.sportstournament.auth.dto.RegisterRequest;
import com.antonio.sportstournament.auth.dto.AuthResult;
import com.antonio.sportstournament.auth.dto.ResendCodeRequest;
import com.antonio.sportstournament.auth.dto.VerifyTwoFactorRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for logging in and registering. Base path: {@code /api/auth}. All of them are public (no token needed).
 * <ul>
 *   <li>{@code POST /api/auth/register} - create a Team Manager account, 201</li>
 *   <li>{@code POST /api/auth/login} - step 1: email and password; returns a JWT or a 2FA challenge, 200 or 401</li>
 *   <li>{@code POST /api/auth/verify-2fa} - step 2: emailed 6-digit code; returns the JWT, 200 or 401</li>
 *   <li>{@code POST /api/auth/resend-2fa} - send a new code, 200, 401, 409 (too soon) or 503 (email failed)</li>
 * </ul>
 * Validation errors on any request body give 400.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;


    /**
     * @param service the authentication business logic
     */
    public AuthController(AuthService service){
        this.service=service;
    }

    /**
     * {@code POST /api/auth/register} - public. Always creates a TEAM_MANAGER (the role cannot be chosen).
     *
     * @param request validated name, email and password
     * @return the new user with a JWT (201 Created); 400 if validation fails, 409 if the email already exists
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse register(@Valid @RequestBody RegisterRequest request){
        return service.register(request);
    }

    /**
     * {@code POST /api/auth/login} - public. Checks email and password.
     *
     * @param request validated email and password
     * @return a JWT right away (200 OK), or {@code twoFactorRequired=true} with a challenge token when a
     *         code was emailed; 401 for wrong credentials, 503 if the email could not be sent
     */
    @PostMapping("/login")
    public AuthResult login(@Valid @RequestBody LoginRequest request){
        return service.login(request);
    }

    /**
     * {@code POST /api/auth/verify-2fa} - public. Completes a login with the emailed 6-digit code.
     *
     * @param request challenge token and 6-digit code
     * @return the user with a JWT (200 OK); 400 if the code is not 6 digits, 401 if wrong or expired
     */
    @PostMapping("/verify-2fa")
    public LoginResponse verifyTwoFactor(@Valid @RequestBody VerifyTwoFactorRequest request) {
        return service.verifyTwoFactor(request);
    }

    /**
     * {@code POST /api/auth/resend-2fa} - public. Emails a new code for an open challenge.
     *
     * @param request the challenge token
     * @return a new challenge (200 OK); 401 if the challenge expired, 409 if requested before the cool-down
     *         ends, 503 if the email could not be sent
     */
    @PostMapping("/resend-2fa")
    public AuthResult resendTwoFactor(@Valid @RequestBody ResendCodeRequest request) {
        return service.resendTwoFactor(request);
    }
}
