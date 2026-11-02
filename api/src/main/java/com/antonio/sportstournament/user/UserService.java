package com.antonio.sportstournament.user;

import com.antonio.sportstournament.exception.ConflictException;
import com.antonio.sportstournament.exception.ForbiddenException;
import com.antonio.sportstournament.exception.ResourceNotFoundException;
import com.antonio.sportstournament.user.dto.UserCreateRequest;
import com.antonio.sportstournament.user.dto.UserResponse;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

/**
 * Business logic for user accounts.
 * <ul>
 *   <li>Rule 1: an email can be used by only one account (compared case-insensitively) - otherwise 409.</li>
 *   <li>Rule 2: passwords are stored only as BCrypt hashes.</li>
 *   <li>Rule 3: ADMIN and ORGANIZER accounts must keep email 2FA on; it cannot be turned off.</li>
 *   <li>Rule 4: a user can change only their own 2FA setting (403) and an admin cannot delete their own account (409).</li>
 * </ul>
 */
@Service
public class UserService {
     private final UserRepository repository;
     private final UserMapper mapper;
     private final PasswordEncoder passwordEncoder;

     public UserService(UserRepository repository,UserMapper mapper,PasswordEncoder passwordEncoder){
         this.repository=repository;
         this.mapper = mapper;
         this.passwordEncoder = passwordEncoder;
     }

     /**
      * Creates a user. The email is trimmed and lower-cased, the password is hashed, and 2FA is on
      * for ADMIN and ORGANIZER (see {@code UserMapper.toEntity}).
      *
      * @param request name, email, password and role
      * @return the saved user
      * @throws ConflictException if the email already exists (409)
      */
     @Transactional
     public UserResponse create(UserCreateRequest request){
         String email = request.email().trim().toLowerCase();

         if(repository.existsByEmailIgnoreCase(email)){
             throw new ConflictException("An account with this email already exists");
         }

          //Take the password that the user entered, hash it
         String hash = passwordEncoder.encode(request.password());
         User user = mapper.toEntity(request.name().trim(),email,hash,request.role());
         return  mapper.toResponse(repository.save(user));
     }

     /**
      * Lists every user.
      *
      * @return all users as responses (no password hash)
      */
     //This database transaction is intended only for reading data, not modifying it.
     @Transactional(readOnly = true)
     public List<UserResponse> findAll(){
         return repository.findAll().stream().map(mapper::toResponse).toList();
     }

    /**
     * Finds one user.
     *
     * @param id the user id
     * @return the user
     * @throws ResourceNotFoundException if no user has this id (404)
     */
    @Transactional(readOnly = true)
     public UserResponse findById(Long id){
         return mapper.toResponse(findEntity(id));
     }

     /**
      * Turns email two-factor login on or off for the caller's own account.
      *
      * @param caller  the logged-in user
      * @param id      the user being changed; must equal the caller's id
      * @param enabled the new setting
      * @return the updated user
      * @throws ForbiddenException        if {@code id} is not the caller's id (403)
      * @throws ConflictException         if an ADMIN or ORGANIZER tries to disable 2FA (409)
      * @throws ResourceNotFoundException if the user no longer exists (404)
      */
     @Transactional
     public UserResponse updateTwoFactor(User caller, Long id, boolean enabled){
         if (!caller.getId().equals(id)) {
             throw new ForbiddenException("You can only change your own security settings.");
         }
         User user = findEntity(id);
         if (!enabled && (user.getRole() == Role.ADMIN || user.getRole() == Role.ORGANIZER)) {
             throw new ConflictException("2FA is required for Administrators and Organizers");
         }
         user.setTwoFactorEnabled(enabled);
         return mapper.toResponse(repository.save(user));
     }

    /**
     * Deletes a user (the user's old 2FA challenges are deleted with it by the database).
     *
     * @param caller the logged-in admin
     * @param id     the user to delete
     * @throws ConflictException         if the caller tries to delete their own account (409)
     * @throws ResourceNotFoundException if the user does not exist (404)
     *                                   (if the user still owns teams or tournaments, the database
     *                                   refuses the delete and the exception handler answers 409)
     */
    @Transactional
     public void delete(User caller, Long id){
         if (caller.getId().equals(id)) {
             throw new ConflictException("You cannot delete your own account.");
         }
         User user = findEntity(id);
         repository.delete(user);
     }

    /** The profile of the logged-in user (GET /api/users/me). */
    public UserResponse toResponse(User user) {
        return mapper.toResponse(user);
    }



    /**
     * Helpers
     *
     * @param id the user id
     * @return the user entity
     * @throws ResourceNotFoundException if no user has this id (404)
     */
    public User findEntity(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"+id));
    }
}
