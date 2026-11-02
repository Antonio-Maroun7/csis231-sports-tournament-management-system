package com.antonio.sportstournament.security;

import com.antonio.sportstournament.exception.ResourceNotFoundException;
import com.antonio.sportstournament.exception.UnauthorizedException;
import com.antonio.sportstournament.user.User;
import com.antonio.sportstournament.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gives controllers the User entity of the caller, so services can apply ownership rules. */
@Service
public class CurrentUserService {
    private final UserRepository userRepository;

    /**
     * @param userRepository user table access
     */
    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Rule: the owner of a team or tournament always comes from the JWT through this method, never from the request.
     *
     * @param authentication the authentication of the current request
     * @return the database user whose email is the JWT subject
     * @throws UnauthorizedException     if there is no authenticated caller (401)
     * @throws ResourceNotFoundException if the user no longer exists (404)
     */
    @Transactional(readOnly = true)
    public User require(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Please log in again.");
        }
        // the authentication name is the email (the JWT subject)
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
