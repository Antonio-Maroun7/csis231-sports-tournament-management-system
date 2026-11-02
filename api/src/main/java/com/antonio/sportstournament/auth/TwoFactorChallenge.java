package com.antonio.sportstournament.auth;

import com.antonio.sportstournament.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

/**
 * Persisted, single-use email 2FA challenge (table {@code two_factor_challenges}) containing only a BCrypt code hash.
 * One row is one emailed code; many challenges can belong to one user, but only the newest unused one is valid.
 */
@Entity
@Table(name = "two_factor_challenges")
@Getter
@Setter
public class TwoFactorChallenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id")
    private Long id;

    // deleting a user also deletes their old 2FA challenges (otherwise the delete fails with 409)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    // Random UUID sent to the client; unique. The client sends it back with the code.
    @Column(nullable = false, unique = true, length = 36)
    private String token;

    // BCrypt hash of the 6-digit code (the code itself is never stored).
    @Column(nullable = false, length = 100)
    private String codeHash;

    // After this time the code is rejected.
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // Number of wrong codes entered so far.
    @Column(nullable = false)
    private int attempts = 0;

    // True once verified, replaced, burned by too many wrong tries, or invalidated by a newer challenge.
    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
