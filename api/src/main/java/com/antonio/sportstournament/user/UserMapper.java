package com.antonio.sportstournament.user;

import com.antonio.sportstournament.auth.dto.LoginResponse;
import com.antonio.sportstournament.user.dto.UserResponse;
import org.springframework.stereotype.Component;

/**
 * Converts between {@link User} entities and the user/login DTOs ({@code UserResponse}, {@code LoginResponse}).
 * The password hash is never copied into a response.
 */
@Component
public class UserMapper {

    /**
     * Builds a new {@link User} entity (not yet saved).
     * Rule: 2FA is switched on automatically for ADMIN and ORGANIZER; TEAM_MANAGER starts with it off.
     *
     * @param name         display name
     * @param email        email, already trimmed and lower-cased by the caller
     * @param passwordHash the BCrypt hash, not the plain password
     * @param role         the role of the new user
     * @return a new, unsaved user
     */
    public User toEntity(String name, String email, String passwordHash, Role role){
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        user.setTwoFactorEnabled(role == Role.ADMIN || role == Role.ORGANIZER);
        return user;
    }

    /**
     * Converts a user to the response returned by {@code /api/users} endpoints.
     *
     * @param user the entity
     * @return id, name, email, role, 2FA flag and creation time
     */
    public UserResponse toResponse(User user){
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isTwoFactorEnabled(),
                user.getCreatedAt()
        );
    }

    /**
     * Converts a user to a {@code LoginResponse} without a token (token is {@code null}).
     *
     * @param user the entity
     * @return the login response with {@code token = null}
     */
    public LoginResponse toLoginResponse(User user){
        return toLoginResponse(user, null);
    }

    /**
     * Converts a user to a {@code LoginResponse} that carries the JWT.
     *
     * @param user  the entity
     * @param token the signed JWT for the {@code Authorization: Bearer} header
     * @return id, name, email, role name, 2FA flag and the token
     */
    public LoginResponse toLoginResponse(User user, String token){
        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.isTwoFactorEnabled(),
                token
        );
    }

}
