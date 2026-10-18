package com.antonio.sportstournament.auth;

import com.antonio.sportstournament.auth.dto.LoginRequest;
import com.antonio.sportstournament.auth.dto.LoginResponse;
import com.antonio.sportstournament.auth.dto.RegisterRequest;
import com.antonio.sportstournament.exception.UnauthorizedException;
import com.antonio.sportstournament.user.Role;
import com.antonio.sportstournament.user.User;
import com.antonio.sportstournament.user.UserMapper;
import com.antonio.sportstournament.user.UserRepository;
import com.antonio.sportstournament.user.UserService;
import com.antonio.sportstournament.user.dto.UserCreateRequest;
import com.antonio.sportstournament.user.dto.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
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

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request){
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(()-> new UnauthorizedException("Invalid email or password"));

        if(!passwordEncoder.matches(request.password(), user.getPasswordHash())){
            throw new UnauthorizedException("Invalid email or password");
        }

        return mapper.toLoginResponse(user);
    }

}
