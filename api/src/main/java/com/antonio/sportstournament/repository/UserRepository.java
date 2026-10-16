package com.antonio.sportstournament.repository;

import com.antonio.sportstournament.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository {


    Optional<User> findByEmailIgnoreCase(String  email);

    // "email already exists" check
    boolean existsByEmailIgnoreCase(String email);
}
