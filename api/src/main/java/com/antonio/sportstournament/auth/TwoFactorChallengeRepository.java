package com.antonio.sportstournament.auth;

import com.antonio.sportstournament.user.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/** Repository for persisted email 2FA challenges. */
public interface TwoFactorChallengeRepository extends JpaRepository<TwoFactorChallenge, Long> {
    Optional<TwoFactorChallenge> findByToken(String token);

    @Modifying
    @Query("update TwoFactorChallenge c set c.used = true where c.user = :user and c.used = false")
    int markUnusedChallengesUsed(@Param("user") User user);
}
