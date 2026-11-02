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

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final UserMapper mapper;
    private final TwoFactorService twoFactorService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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

    /** Creates a Team Manager and logs them in directly (new accounts have no 2FA). */
    @Transactional
    public LoginResponse register(RegisterRequest request){
        UserResponse created = userService.create(new UserCreateRequest(
                request.name(), request.email(), request.password(), Role.TEAM_MANAGER)
        );
        return new LoginResponse(created.id(), created.name(), created.email(), created.role().name(),
                created.twoFactorEnabled(), jwtService.generateToken(created.email()));
    }

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

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public LoginResponse verifyTwoFactor(VerifyTwoFactorRequest request) {
        return twoFactorService.verify(request);
    }

    @Transactional
    public AuthResult resendTwoFactor(ResendCodeRequest request) {
        return twoFactorService.resend(request);
    }

}
