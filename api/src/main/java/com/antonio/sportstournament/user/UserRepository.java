package com.antonio.sportstournament.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {


    Optional<User> findByEmailIgnoreCase(String  email);

    // "email already exists" check
    boolean existsByEmailIgnoreCase(String email);
}
