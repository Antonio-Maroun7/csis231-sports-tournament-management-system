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

     //This database transaction is intended only for reading data, not modifying it.
     @Transactional(readOnly = true)
     public List<UserResponse> findAll(){
         return repository.findAll().stream().map(mapper::toResponse).toList();
     }

    @Transactional(readOnly = true)
     public UserResponse findById(Long id){
         return mapper.toResponse(findEntity(id));
     }

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
     */
    public User findEntity(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"+id));
    }
}
