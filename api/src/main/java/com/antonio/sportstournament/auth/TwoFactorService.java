package com.antonio.sportstournament.auth;

import com.antonio.sportstournament.auth.dto.AuthResult;
import com.antonio.sportstournament.auth.dto.ResendCodeRequest;
import com.antonio.sportstournament.auth.dto.VerifyTwoFactorRequest;
import com.antonio.sportstournament.email.EmailService;
import com.antonio.sportstournament.exception.ConflictException;
import com.antonio.sportstournament.exception.UnauthorizedException;
import com.antonio.sportstournament.security.JwtService;
import com.antonio.sportstournament.user.User;
import com.antonio.sportstournament.user.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Creates and verifies single-use email-based two-factor login challenges. */
@Service
public class TwoFactorService {
    private static final String EXPIRED_MESSAGE = "This code has expired. Please log in again.";
    private final TwoFactorChallengeRepository repository;
    private final EmailService emailService;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecureRandom random = new SecureRandom();
    private final int validityMinutes;
    private final int maxAttempts;
    private final int resendCooldownSeconds;

    public TwoFactorService(TwoFactorChallengeRepository repository, EmailService emailService,
                            UserMapper mapper, PasswordEncoder passwordEncoder, JwtService jwtService,
                            @Value("${app.two-factor.code-validity-minutes:5}") int validityMinutes,
                            @Value("${app.two-factor.max-attempts:5}") int maxAttempts,
                            @Value("${app.two-factor.resend-cooldown-seconds:60}") int resendCooldownSeconds) {
        this.repository = repository;
        this.emailService = emailService;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.validityMinutes = validityMinutes;
        this.maxAttempts = maxAttempts;
        this.resendCooldownSeconds = resendCooldownSeconds;
    }

    @Transactional
    public AuthResult createChallenge(User user) {
        repository.markUnusedChallengesUsed(user);
        return createAndSend(user);
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public com.antonio.sportstournament.auth.dto.LoginResponse verify(VerifyTwoFactorRequest request) {
        TwoFactorChallenge challenge = findChallenge(request.challengeToken());
        if (challenge.isUsed() || challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException(EXPIRED_MESSAGE);
        }
        if (!passwordEncoder.matches(request.code(), challenge.getCodeHash())) {
            challenge.setAttempts(challenge.getAttempts() + 1);
            if (challenge.getAttempts() >= maxAttempts) {
                challenge.setUsed(true);
                throw new UnauthorizedException("Too many wrong codes. Please log in again.");
            }
            int left = maxAttempts - challenge.getAttempts();
            throw new UnauthorizedException("Invalid code. " + left + " attempts left.");
        }
        challenge.setUsed(true);
        User user = challenge.getUser();
        return mapper.toLoginResponse(user, jwtService.generateToken(user.getEmail()));
    }

    @Transactional
    public AuthResult resend(ResendCodeRequest request) {
        TwoFactorChallenge oldChallenge = findChallenge(request.challengeToken());
        if (oldChallenge.isUsed() || oldChallenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException(EXPIRED_MESSAGE);
        }
        long elapsed = ChronoUnit.SECONDS.between(oldChallenge.getCreatedAt(), LocalDateTime.now());
        if (elapsed < resendCooldownSeconds) {
            throw new ConflictException("Please wait " + (resendCooldownSeconds - elapsed)
                    + " seconds before requesting a new code.");
        }
        oldChallenge.setUsed(true);
        return createAndSend(oldChallenge.getUser());
    }

    private AuthResult createAndSend(User user) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        LocalDateTime createdAt = LocalDateTime.now();
        TwoFactorChallenge challenge = new TwoFactorChallenge();
        challenge.setUser(user);
        challenge.setToken(UUID.randomUUID().toString());
        challenge.setCodeHash(passwordEncoder.encode(code));
        challenge.setCreatedAt(createdAt);
        challenge.setExpiresAt(createdAt.plusMinutes(validityMinutes));
        TwoFactorChallenge saved = repository.save(challenge);
        emailService.sendLoginCode(user.getEmail(), user.getName(), code, validityMinutes);
        return new AuthResult(true, saved.getToken(), maskEmail(user.getEmail()), saved.getExpiresAt(), null);
    }

    private TwoFactorChallenge findChallenge(String token) {
        return repository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException(EXPIRED_MESSAGE));
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String visible = local.substring(0, Math.min(2, local.length()));
        return visible + "***" + email.substring(at);
    }
}
