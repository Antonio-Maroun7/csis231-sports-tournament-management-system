package com.antonio.sportstournament.auth;

import com.antonio.sportstournament.user.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/** Repository for the {@code two_factor_challenges} table (persisted email 2FA challenges). */
public interface TwoFactorChallengeRepository extends JpaRepository<TwoFactorChallenge, Long> {
    /**
     * Finds the challenge with this token.
     *
     * @param token the challenge token sent by the client
     * @return the challenge, or empty if the token is unknown
     */
    Optional<TwoFactorChallenge> findByToken(String token);

    /**
     * Marks every still-unused challenge of this user as used, so only the newest code works.
     *
     * @param user the user who is starting a new login
     * @return how many challenges were updated
     */
    @Modifying
    @Query("update TwoFactorChallenge c set c.used = true where c.user = :user and c.used = false")
    int markUnusedChallengesUsed(@Param("user") User user);
}
