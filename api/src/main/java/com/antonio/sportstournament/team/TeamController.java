package com.antonio.sportstournament.team;

import com.antonio.sportstournament.security.CurrentUserService;
import com.antonio.sportstournament.team.dto.TeamRequest;
import com.antonio.sportstournament.team.dto.TeamResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for teams: /api/teams
 * <ul>
 *   <li>POST   /api/teams       - create a team (TEAM_MANAGER or ADMIN), 201</li>
 *   <li>GET    /api/teams       - list all teams, or the teams of one manager with {@code ?managerId=}</li>
 *   <li>GET    /api/teams/{id}  - one team</li>
 *   <li>PUT    /api/teams/{id}  - rename a team (its manager or ADMIN)</li>
 *   <li>DELETE /api/teams/{id}  - delete a team (its manager or ADMIN), 204</li>
 * </ul>
 * Every endpoint needs a valid JWT (401 otherwise). Ownership rules are checked in {@link TeamService}.
 */
@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService service;
    private final CurrentUserService currentUserService;

    public TeamController(TeamService service, CurrentUserService currentUserService) {
        this.service = service;
        this.currentUserService = currentUserService;
    }

    /**
     * {@code POST /api/teams} - creates a team owned by the caller. Allowed roles: TEAM_MANAGER, ADMIN
     * (enforced by SecurityConfig and again by the service). The manager comes from the JWT, never from the body.
     *
     * @param request        the team name
     * @param authentication the logged-in caller, injected by Spring Security
     * @return the new team, status 201 Created
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse create(@Valid @RequestBody TeamRequest request, Authentication authentication) {
        return service.create(currentUserService.require(authentication), request);
    }

    /**
     * {@code GET /api/teams} - any logged-in user. Returns all teams, or only the teams of one manager
     * when {@code managerId} is given (used by "My teams"). Status 200; 404 if the managerId user does not exist.
     * <pre>
     * GET /api/teams              : all teams
     * GET /api/teams?managerId=1  : teams of one Team Manager
     * </pre>
     *
     * @param managerId optional id of the manager whose teams are wanted
     * @return the teams ordered by name
     */
    @GetMapping
    public List<TeamResponse> findAll(@RequestParam(required = false) Long managerId) {
        return managerId == null ? service.findAll() : service.findByManager(managerId);
    }

    /**
     * {@code GET /api/teams/{id}} - any logged-in user. Status 200, or 404 if the team does not exist.
     *
     * @param id the team id
     * @return the team
     */
    @GetMapping("/{id}")
    public TeamResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    /**
     * {@code PUT /api/teams/{id}} - renames a team. Only the team's manager or an ADMIN.
     * Status 200; 400 invalid name, 403 not the owner, 404 unknown team.
     *
     * @param id             the team id
     * @param request        the new team name
     * @param authentication the logged-in caller
     * @return the updated team
     */
    @PutMapping("/{id}")
    public TeamResponse update(@PathVariable Long id, @Valid @RequestBody TeamRequest request,
                               Authentication authentication) {
        return service.update(currentUserService.require(authentication), id, request);
    }

    /**
     * {@code DELETE /api/teams/{id}} - deletes a team. Only the team's manager or an ADMIN.
     * Status 204; 403 not the owner, 404 unknown team, 409 if the team still has players or registrations.
     *
     * @param id             the team id
     * @param authentication the logged-in caller
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.delete(currentUserService.require(authentication), id);
    }
}