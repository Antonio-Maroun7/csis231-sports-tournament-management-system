package com.antonio.sportstournament.controller;

import com.antonio.sportstournament.dto.UserCreateRequest;
import com.antonio.sportstournament.dto.UserResponse;
import com.antonio.sportstournament.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service){
        this.service=service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserCreateRequest request){
         return service.create(request);
    }

    @GetMapping
    public List<UserResponse> findAll(){
        return service.findAll();
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id){
        return service.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
         service.delete(id);
    }

}
