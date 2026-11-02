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
 * REST endpoints for players.
 *   GET    /api/teams/{teamId}/players   roster of a team
 *   POST   /api/teams/{teamId}/players   add a player to a team
 *   GET    /api/players/{id}
 *   PUT    /api/players/{id}
 *   DELETE /api/players/{id}
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

    @GetMapping("/teams/{teamId}/players")
    public List<PlayerResponse> findByTeam(@PathVariable Long teamId) {
        return service.findByTeam(teamId);
    }

    @PostMapping("/teams/{teamId}/players")
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerResponse add(@PathVariable Long teamId, @Valid @RequestBody PlayerRequest request,
                              Authentication authentication) {
        return service.add(currentUserService.require(authentication), teamId, request);
    }

    @GetMapping("/players/{id}")
    public PlayerResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/players/{id}")
    public PlayerResponse update(@PathVariable Long id, @Valid @RequestBody PlayerRequest request,
                                 Authentication authentication) {
        return service.update(currentUserService.require(authentication), id, request);
    }

    @DeleteMapping("/players/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long id, Authentication authentication) {
        service.remove(currentUserService.require(authentication), id);
    }
}