package com.antonio.sportstournament.user;

import com.antonio.sportstournament.security.CurrentUserService;
import com.antonio.sportstournament.user.dto.UserCreateRequest;
import com.antonio.sportstournament.user.dto.UserResponse;
import com.antonio.sportstournament.user.dto.TwoFactorSettingRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;
    private final CurrentUserService currentUserService;

    public UserController(UserService service, CurrentUserService currentUserService){
        this.service=service;
        this.currentUserService = currentUserService;
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

    // GET /api/users/me -> profile of the logged-in user
    @GetMapping("/me")
    public UserResponse me(Authentication authentication){
        return service.toResponse(currentUserService.require(authentication));
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id){
        return service.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication){
         service.delete(currentUserService.require(authentication), id);
    }

    @PutMapping("/{id}/two-factor")
    public UserResponse updateTwoFactor(@PathVariable Long id,
                                        @Valid @RequestBody TwoFactorSettingRequest request,
                                        Authentication authentication){
        return service.updateTwoFactor(currentUserService.require(authentication), id, request.enabled());
    }

}
