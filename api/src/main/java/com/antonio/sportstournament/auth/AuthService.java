package com.antonio.sportstournament.auth;

import com.antonio.sportstournament.auth.dto.LoginRequest;
import com.antonio.sportstournament.auth.dto.LoginResponse;
import com.antonio.sportstournament.auth.dto.AuthResult;
import com.antonio.sportstournament.auth.dto.ResendCodeRequest;
import com.antonio.sportstournament.auth.dto.VerifyTwoFactorRequest;
import com.antonio.sportstournament.auth.dto.RegisterRequest;
import com.antonio.sportstournament.exception.UnauthorizedException;
import com.antonio.sportstournament.security.JwtService;
import com.antonio.sportstournament.user.Role;
import com.antonio.sportstournament.user.User;
import com.antonio.sportstournament.user.UserMapper;
import com.antonio.sportstournament.user.UserRepository;
import com.antonio.sportstournament.user.UserService;
import com.antonio.sportstournament.user.dto.UserCreateRequest;
import com.antonio.sportstournament.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for logging in and registering.
 * <ul>
 *   <li>Rule 1: public registration always creates a TEAM_MANAGER and logs them in directly (no 2FA yet).</li>
 *   <li>Rule 2: wrong email and wrong password give the same 401 message, so attackers cannot learn which emails exist.</li>
 *   <li>Rule 3: ADMIN, ORGANIZER and any user with 2FA enabled get no JWT until the emailed 6-digit code is verified.</li>
 * </ul>
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final UserMapper mapper;
    private final TwoFactorService twoFactorService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * @param repository       user table access
     * @param service          creates the user (hash, duplicate-email check)
     * @param mapper           builds login responses
     * @param twoFactorService creates, resends and verifies email codes
     * @param passwordEncoder  checks the password against the stored hash
     * @param jwtService       signs the JWT
     */
    public AuthService(UserRepository repository,UserService service,UserMapper mapper,
                       TwoFactorService twoFactorService, PasswordEncoder passwordEncoder,
                       JwtService jwtService){
        this.userRepository=repository;
        this.userService=service;
        this.mapper=mapper;
        this.twoFactorService = twoFactorService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Creates a Team Manager and logs them in directly (new accounts have no 2FA).
     *
     * @param request name, email and password
     * @return the new user together with a JWT
     * @throws com.antonio.sportstournament.exception.ConflictException if the email is already registered (409)
     */
    @Transactional
    public LoginResponse register(RegisterRequest request){
        UserResponse created = userService.create(new UserCreateRequest(
                request.name(), request.email(), request.password(), Role.TEAM_MANAGER)
        );
        return new LoginResponse(created.id(), created.name(), created.email(), created.role().name(),
                created.twoFactorEnabled(), jwtService.generateToken(created.email()));
    }

    /**
     * Step 1 of login: checks the email and password.
     * ADMIN, ORGANIZER and users with 2FA enabled receive an email challenge instead of a token;
     * everyone else gets the JWT immediately.
     *
     * @param request email and password
     * @return {@code twoFactorRequired=true} with a challenge token, or a completed login with a JWT
     * @throws UnauthorizedException if the email or password is wrong (401)
     * @throws com.antonio.sportstournament.exception.EmailDeliveryException if the code email cannot be sent (503)
     */
    @Transactional
    public AuthResult login(LoginRequest request){
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(()-> new UnauthorizedException("Invalid email or password"));

        if(!passwordEncoder.matches(request.password(), user.getPasswordHash())){
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getRole() == Role.ADMIN || user.getRole() == Role.ORGANIZER || user.isTwoFactorEnabled()) {
            return twoFactorService.createChallenge(user);   // no token until the code is verified
        }
        return new AuthResult(false, null, null, null,
                mapper.toLoginResponse(user, jwtService.generateToken(user.getEmail())));
    }

    /**
     * Step 2 of login: checks the 6-digit emailed code and returns the JWT.
     * {@code noRollbackFor} keeps the wrong-attempt counter saved even though a 401 is thrown.
     *
     * @param request challenge token and 6-digit code
     * @return the user with a JWT
     * @throws UnauthorizedException if the code is wrong, expired, already used or too many attempts were made (401)
     */
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public LoginResponse verifyTwoFactor(VerifyTwoFactorRequest request) {
        return twoFactorService.verify(request);
    }

    /**
     * Sends a new code for an existing challenge (the old one is invalidated).
     *
     * @param request the challenge token from the login response
     * @return a new challenge
     * @throws UnauthorizedException if the challenge is unknown, used or expired (401)
     * @throws com.antonio.sportstournament.exception.ConflictException if the resend cool-down has not passed (409)
     * @throws com.antonio.sportstournament.exception.EmailDeliveryException if the email cannot be sent (503)
     */
    @Transactional
    public AuthResult resendTwoFactor(ResendCodeRequest request) {
        return twoFactorService.resend(request);
    }

}
