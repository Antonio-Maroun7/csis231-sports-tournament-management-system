package com.antonio.sportstournament.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Creates and validates the signed JWTs (HS256) that the clients send in the Authorization header.
 * The subject of a token is the user's email.
 */
@Service
public class JwtService {
    private static final int MIN_SECRET_BYTES = 32;   // HS256 needs a 256-bit key

    private final SecretKey signingKey;
    private final JwtParser parser;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") long expirationMs) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret (JWT_SECRET in api/.env) must be at least 32 characters long.");
        }
        if (secret.startsWith("change_me")) {
            throw new IllegalStateException("JWT_SECRET in api/.env is still the example value. Generate a random one.");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.parser = Jwts.parser().verifyWith(signingKey).build();   // also rejects expired tokens
        this.expirationMs = expirationMs;
    }

    public String generateToken(String subject) {
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Returns the subject (the email) of a valid token.
     *
     * @throws JwtException             if the token is tampered, expired or unsupported
     * @throws IllegalArgumentException if the token is empty or malformed
     */
    public String extractUsername(String token) {
        return parser.parseSignedClaims(token).getPayload().getSubject();
    }
}
