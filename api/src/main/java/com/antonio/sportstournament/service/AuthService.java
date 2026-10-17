package com.antonio.sportstournament.service;

import com.antonio.sportstournament.dto.*;
import com.antonio.sportstournament.exception.UnauthorizedException;
import com.antonio.sportstournament.mapper.UserMapper;
import com.antonio.sportstournament.model.Role;
import com.antonio.sportstournament.model.User;
import com.antonio.sportstournament.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final UserMapper mapper;
    private final  BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository repository,UserService service,UserMapper mapper){
        this.userRepository=repository;
        this.userService=service;
        this.mapper=mapper;
    }

    @Transactional
    public UserResponse register(RegisterRequest request){
        return userService.create(new UserCreateRequest(
                request.name(), request.email(), request.password(), Role.TEAM_MANAGER)
        );
    }

    public LoginResponse login(LoginRequest request){
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(()-> new UnauthorizedException("Invalid email or password"));

        if(!passwordEncoder.matches(request.password(), user.getPasswordHash())){
            throw new UnauthorizedException("Invalid email or password");
        }

        return mapper.toLoginResponse(user);
    }

}
