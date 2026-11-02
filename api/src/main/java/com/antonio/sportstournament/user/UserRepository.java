package com.antonio.sportstournament.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Repository for the {@code users} table. */
public interface UserRepository extends JpaRepository<User,Long> {


    /**
     * Finds the user with this email, ignoring upper/lower case (used by login and the JWT filter).
     *
     * @param email the email to look for
     * @return the matching user, or empty if there is none
     */
    Optional<User> findByEmailIgnoreCase(String  email);

    // "email already exists" check
    boolean existsByEmailIgnoreCase(String email);

    // used by the bootstrap admin
    boolean existsByRole(Role role);
}
