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
 * REST endpoints for team registrations (base path {@code /api}). Every endpoint needs a valid JWT (401 otherwise).
 * <pre>
 *   GET    /api/tournaments/{tournamentId}/registrations   teams registered in a tournament (any logged-in user)
 *   POST   /api/tournaments/{tournamentId}/registrations   register a team   {"teamId": 3}  (TEAM_MANAGER or ADMIN), 201
 *   GET    /api/teams/{teamId}/registrations               tournaments a team registered for (any logged-in user)
 *   PUT    /api/registrations/{id}/approve                 Organizer approves (ORGANIZER or ADMIN)
 *   PUT    /api/registrations/{id}/reject                  Organizer rejects (ORGANIZER or ADMIN)
 *   DELETE /api/registrations/{id}                         team withdraws (team's manager or ADMIN), 204
 * </pre>
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

    /**
     * {@code GET /api/tournaments/{tournamentId}/registrations} - any logged-in user. Status 200;
     * 404 if the tournament does not exist.
     *
     * @param tournamentId the tournament id
     * @return its registrations, oldest first
     */
    @GetMapping("/tournaments/{tournamentId}/registrations")
    public List<RegistrationResponse> findByTournament(@PathVariable Long tournamentId) {
        return service.findByTournament(tournamentId);
    }

    /**
     * {@code POST /api/tournaments/{tournamentId}/registrations} - registers a team in a tournament.
     * Allowed roles: TEAM_MANAGER, ADMIN (SecurityConfig); a TEAM_MANAGER may only register his own team.
     * The new registration is PENDING. Status 201; 400 missing teamId, 403 not the team's manager,
     * 404 unknown tournament or team, 409 tournament not OPEN or team already registered.
     *
     * @param tournamentId   the tournament id (from the URL)
     * @param request        the id of the team to register
     * @param authentication the logged-in caller
     * @return the new registration
     */
    @PostMapping("/tournaments/{tournamentId}/registrations")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@PathVariable Long tournamentId,
                                         @Valid @RequestBody RegistrationRequest request,
                                         Authentication authentication) {
        return service.register(currentUserService.require(authentication), tournamentId, request.teamId());
    }

    /**
     * {@code GET /api/teams/{teamId}/registrations} - any logged-in user. Status 200; 404 if the team does not exist.
     *
     * @param teamId the team id
     * @return the team's registrations, newest first
     */
    @GetMapping("/teams/{teamId}/registrations")
    public List<RegistrationResponse> findByTeam(@PathVariable Long teamId) {
        return service.findByTeam(teamId);
    }

    /**
     * {@code PUT /api/registrations/{id}/approve} - approves a PENDING registration. Allowed roles: ORGANIZER, ADMIN
     * (SecurityConfig); an ORGANIZER must own the tournament. Status 200; 403 not the tournament's organizer,
     * 404 unknown registration, 409 not PENDING or tournament COMPLETED.
     *
     * @param id             the registration id
     * @param authentication the logged-in caller
     * @return the registration with status APPROVED
     */
    @PutMapping("/registrations/{id}/approve")
    public RegistrationResponse approve(@PathVariable Long id, Authentication authentication) {
        return service.approve(currentUserService.require(authentication), id);
    }

    /**
     * {@code PUT /api/registrations/{id}/reject} - rejects a PENDING registration. Same roles and errors as approve.
     *
     * @param id             the registration id
     * @param authentication the logged-in caller
     * @return the registration with status REJECTED
     */
    @PutMapping("/registrations/{id}/reject")
    public RegistrationResponse reject(@PathVariable Long id, Authentication authentication) {
        return service.reject(currentUserService.require(authentication), id);
    }

    /**
     * {@code DELETE /api/registrations/{id}} - a team withdraws. Only the team's manager or an ADMIN.
     * Status 204; 403 not the team's manager, 404 unknown registration, 409 tournament no longer OPEN.
     *
     * @param id             the registration id
     * @param authentication the logged-in caller
     */
    @DeleteMapping("/registrations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@PathVariable Long id, Authentication authentication) {
        service.withdraw(currentUserService.require(authentication), id);
    }
}