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

/**
 * REST endpoints for user accounts. Base path: {@code /api/users}.
 * <ul>
 *   <li>{@code POST /api/users} - create a user with any role (ADMIN only), 201</li>
 *   <li>{@code GET /api/users} - list all users (ADMIN only), 200</li>
 *   <li>{@code GET /api/users/me} - profile of the logged-in user (any logged-in user), 200</li>
 *   <li>{@code GET /api/users/{id}} - one user (ADMIN only), 200 or 404</li>
 *   <li>{@code DELETE /api/users/{id}} - delete a user (ADMIN only), 204, 404 or 409</li>
 *   <li>{@code PUT /api/users/{id}/two-factor} - change own email 2FA setting (any logged-in user, own id only), 200, 403 or 409</li>
 * </ul>
 * The URL role rules are defined in {@code SecurityConfig}; 401 is returned when the token is missing or expired.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;
    private final CurrentUserService currentUserService;

    /**
     * @param service            the user business logic
     * @param currentUserService resolves the logged-in user from the JWT authentication
     */
    public UserController(UserService service, CurrentUserService currentUserService){
        this.service=service;
        this.currentUserService = currentUserService;
    }

    /**
     * {@code POST /api/users} - ADMIN only. Creates an account with the role chosen in the request.
     *
     * @param request validated name, email, password and role
     * @return the created user (201 Created); 400 if validation fails, 409 if the email is already used
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserCreateRequest request){
         return service.create(request);
    }

    /**
     * {@code GET /api/users} - ADMIN only.
     *
     * @return all users (200 OK); the password hash is never included
     */
    @GetMapping
    public List<UserResponse> findAll(){
        return service.findAll();
    }

    /**
     * {@code GET /api/users/me} - any logged-in user. The user comes from the JWT, not from the URL.
     *
     * @param authentication the authenticated caller
     * @return the caller's own profile (200 OK); 401 if not logged in
     */
    @GetMapping("/me")
    public UserResponse me(Authentication authentication){
        return service.toResponse(currentUserService.require(authentication));
    }

    /**
     * {@code GET /api/users/{id}} - ADMIN only.
     *
     * @param id the user id
     * @return the user (200 OK); 404 if it does not exist
     */
    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id){
        return service.findById(id);
    }

    /**
     * {@code DELETE /api/users/{id}} - ADMIN only. An admin cannot delete their own account.
     *
     * @param id             the user to delete
     * @param authentication the authenticated admin
     *                       (204 No Content on success; 404 if the user does not exist; 409 if it is the caller's own account)
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication){
         service.delete(currentUserService.require(authentication), id);
    }

    /**
     * {@code PUT /api/users/{id}/two-factor} - any logged-in user, but only for their own id.
     *
     * @param id             the user id (must be the caller's id)
     * @param request        {@code enabled} true or false (required)
     * @param authentication the authenticated caller
     * @return the updated user (200 OK); 403 if the id is not the caller's,
     *         409 if an ADMIN or ORGANIZER tries to turn 2FA off
     */
    @PutMapping("/{id}/two-factor")
    public UserResponse updateTwoFactor(@PathVariable Long id,
                                        @Valid @RequestBody TwoFactorSettingRequest request,
                                        Authentication authentication){
        return service.updateTwoFactor(currentUserService.require(authentication), id, request.enabled());
    }

}
