package com.antonio.sportstournament.controller;

import com.antonio.sportstournament.dto.LoginRequest;
import com.antonio.sportstournament.dto.LoginResponse;
import com.antonio.sportstournament.dto.RegisterRequest;
import com.antonio.sportstournament.dto.UserResponse;
import com.antonio.sportstournament.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;


    public AuthController(AuthService service){
        this.service=service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request){
        return service.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request){
        return service.login(request);
    }
}
