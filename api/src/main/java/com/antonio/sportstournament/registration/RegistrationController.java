package com.antonio.sportstournament.registration;

import com.antonio.sportstournament.registration.dto.RegistrationRequest;
import com.antonio.sportstournament.registration.dto.RegistrationResponse;
import com.antonio.sportstournament.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for team registrations.
 *   GET    /api/tournaments/{tournamentId}/registrations   teams registered in a tournament
 *   POST   /api/tournaments/{tournamentId}/registrations   register a team   {"teamId": 3}
 *   GET    /api/teams/{teamId}/registrations               tournaments a team registered for
 *   PUT    /api/registrations/{id}/approve                 Organizer approves
 *   PUT    /api/registrations/{id}/reject                  Organizer rejects
 *   DELETE /api/registrations/{id}                         team withdraws
 */
@RestController
@RequestMapping("/api")
public class RegistrationController {
    private final RegistrationService service;
    private final CurrentUserService currentUserService;

    public RegistrationController(RegistrationService service, CurrentUserService currentUserService) {
        this.service = service;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/tournaments/{tournamentId}/registrations")
    public List<RegistrationResponse> findByTournament(@PathVariable Long tournamentId) {
        return service.findByTournament(tournamentId);
    }

    @PostMapping("/tournaments/{tournamentId}/registrations")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@PathVariable Long tournamentId,
                                         @Valid @RequestBody RegistrationRequest request,
                                         Authentication authentication) {
        return service.register(currentUserService.require(authentication), tournamentId, request.teamId());
    }

    @GetMapping("/teams/{teamId}/registrations")
    public List<RegistrationResponse> findByTeam(@PathVariable Long teamId) {
        return service.findByTeam(teamId);
    }

    @PutMapping("/registrations/{id}/approve")
    public RegistrationResponse approve(@PathVariable Long id, Authentication authentication) {
        return service.approve(currentUserService.require(authentication), id);
    }

    @PutMapping("/registrations/{id}/reject")
    public RegistrationResponse reject(@PathVariable Long id, Authentication authentication) {
        return service.reject(currentUserService.require(authentication), id);
    }

    @DeleteMapping("/registrations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@PathVariable Long id, Authentication authentication) {
        service.withdraw(currentUserService.require(authentication), id);
    }
}