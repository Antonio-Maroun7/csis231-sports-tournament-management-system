package com.antonio.sportstournament.tournament;

import com.antonio.sportstournament.tournament.dto.TournamentRequest;
import com.antonio.sportstournament.tournament.dto.TournamentResponse;
import com.antonio.sportstournament.tournament.dto.TournamentStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for tournaments: /api/tournaments
 */
@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {
    private final TournamentService service;

    public TournamentController(TournamentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TournamentResponse create(@Valid @RequestBody TournamentRequest request) {
        return service.create(request);
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
    public TournamentResponse update(@PathVariable Long id, @Valid @RequestBody TournamentRequest request) {
        return service.update(id, request);
    }

    @PutMapping("/{id}/status")
    public TournamentResponse changeStatus(@PathVariable Long id,
                                           @Valid @RequestBody TournamentStatusRequest request) {
        return service.changeStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}