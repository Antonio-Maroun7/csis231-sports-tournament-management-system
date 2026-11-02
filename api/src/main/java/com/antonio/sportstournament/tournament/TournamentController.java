package com.antonio.sportstournament.tournament;

import com.antonio.sportstournament.security.CurrentUserService;
import com.antonio.sportstournament.tournament.dto.TournamentRequest;
import com.antonio.sportstournament.tournament.dto.TournamentResponse;
import com.antonio.sportstournament.tournament.dto.TournamentStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for tournaments: /api/tournaments
 */
@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {
    private final TournamentService service;
    private final CurrentUserService currentUserService;

    public TournamentController(TournamentService service, CurrentUserService currentUserService) {
        this.service = service;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TournamentResponse create(@Valid @RequestBody TournamentRequest request,
                                     Authentication authentication) {
        return service.create(currentUserService.require(authentication), request);
    }

    // GET /api/tournaments                 -> all
    // GET /api/tournaments?status=OPEN     -> open for registration
    // GET /api/tournaments?organizerId=2   -> one organizer's tournaments
    @GetMapping
    public List<TournamentResponse> findAll(@RequestParam(required = false) TournamentStatus status,
                                            @RequestParam(required = false) Long organizerId) {
        return service.findAll(status, organizerId);
    }

    @GetMapping("/{id}")
    public TournamentResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public TournamentResponse update(@PathVariable Long id, @Valid @RequestBody TournamentRequest request,
                                     Authentication authentication) {
        return service.update(currentUserService.require(authentication), id, request);
    }

    @PutMapping("/{id}/status")
    public TournamentResponse changeStatus(@PathVariable Long id,
                                           @Valid @RequestBody TournamentStatusRequest request,
                                           Authentication authentication) {
        return service.changeStatus(currentUserService.require(authentication), id, request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.delete(currentUserService.require(authentication), id);
    }
}