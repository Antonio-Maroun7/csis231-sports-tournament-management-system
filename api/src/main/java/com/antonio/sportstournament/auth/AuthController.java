package com.antonio.sportstournament.auth;

import com.antonio.sportstournament.auth.dto.LoginRequest;
import com.antonio.sportstournament.auth.dto.LoginResponse;
import com.antonio.sportstournament.auth.dto.RegisterRequest;
import com.antonio.sportstournament.user.dto.UserResponse;
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
