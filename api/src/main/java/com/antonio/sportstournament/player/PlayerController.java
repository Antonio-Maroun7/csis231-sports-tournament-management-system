package com.antonio.sportstournament.player;

import com.antonio.sportstournament.player.dto.PlayerRequest;
import com.antonio.sportstournament.player.dto.PlayerResponse;
import com.antonio.sportstournament.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for players (base path {@code /api}). Every endpoint needs a valid JWT (401 otherwise).
 * <pre>
 *   GET    /api/teams/{teamId}/players   roster of a team (any logged-in user)
 *   POST   /api/teams/{teamId}/players   add a player to a team (team's manager or ADMIN), 201
 *   GET    /api/players/{id}             one player (any logged-in user)
 *   PUT    /api/players/{id}             edit a player (team's manager or ADMIN)
 *   DELETE /api/players/{id}             remove a player (team's manager or ADMIN), 204
 * </pre>
 * Ownership and jersey-number rules are checked in {@link PlayerService}.
 */
@RestController
@RequestMapping("/api")
public class PlayerController {
    private final PlayerService service;
    private final CurrentUserService currentUserService;

    public PlayerController(PlayerService service, CurrentUserService currentUserService) {
        this.service = service;
        this.currentUserService = currentUserService;
    }

    /**
     * {@code GET /api/teams/{teamId}/players} - any logged-in user. Status 200; 404 if the team does not exist.
     *
     * @param teamId the team id
     * @return the roster ordered by jersey number
     */
    @GetMapping("/teams/{teamId}/players")
    public List<PlayerResponse> findByTeam(@PathVariable Long teamId) {
        return service.findByTeam(teamId);
    }

    /**
     * {@code POST /api/teams/{teamId}/players} - adds a player to a team. Only that team's manager or an ADMIN.
     * Status 201; 400 invalid body, 403 not the team's manager, 404 unknown team, 409 jersey number already used.
     *
     * @param teamId         the team id (from the URL, not the body)
     * @param request        the player data
     * @param authentication the logged-in caller
     * @return the new player
     */
    @PostMapping("/teams/{teamId}/players")
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerResponse add(@PathVariable Long teamId, @Valid @RequestBody PlayerRequest request,
                              Authentication authentication) {
        return service.add(currentUserService.require(authentication), teamId, request);
    }

    /**
     * {@code GET /api/players/{id}} - any logged-in user. Status 200, or 404 if the player does not exist.
     *
     * @param id the player id
     * @return the player
     */
    @GetMapping("/players/{id}")
    public PlayerResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    /**
     * {@code PUT /api/players/{id}} - edits a player. Only the manager of the player's team or an ADMIN.
     * Status 200; 400 invalid body, 403 not the owner, 404 unknown player, 409 jersey number used by another player.
     *
     * @param id             the player id
     * @param request        the new player data
     * @param authentication the logged-in caller
     * @return the updated player
     */
    @PutMapping("/players/{id}")
    public PlayerResponse update(@PathVariable Long id, @Valid @RequestBody PlayerRequest request,
                                 Authentication authentication) {
        return service.update(currentUserService.require(authentication), id, request);
    }

    /**
     * {@code DELETE /api/players/{id}} - removes a player. Only the manager of the player's team or an ADMIN.
     * Status 204; 403 not the owner, 404 unknown player.
     *
     * @param id             the player id
     * @param authentication the logged-in caller
     */
    @DeleteMapping("/players/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long id, Authentication authentication) {
        service.remove(currentUserService.require(authentication), id);
    }
}