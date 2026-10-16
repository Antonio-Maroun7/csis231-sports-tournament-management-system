package com.antonio.sportstournament.mapper;

import com.antonio.sportstournament.dto.LoginResponse;
import com.antonio.sportstournament.dto.UserResponse;
import com.antonio.sportstournament.model.Role;
import com.antonio.sportstournament.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(String name, String email, String passwordHash, Role role){
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setRole(role);
        return user;
    }

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

    public LoginResponse toLoginResponse(User user){
        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.isTwoFactorEnabled()

        );
    }

}
