package com.antonio.sportstournament.team;

import com.antonio.sportstournament.team.dto.TeamRequest;
import com.antonio.sportstournament.team.dto.TeamResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for teams: /api/teams
 */
@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamService service;

    public TeamController(TeamService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse create(@Valid @RequestBody TeamRequest request) {
        return service.create(request);
    }

    // GET /api/teams              -> all teams
    // GET /api/teams?managerId=1  -> teams of one Team Manager
    @GetMapping
    public List<TeamResponse> findAll(@RequestParam(required = false) Long managerId) {
        return managerId == null ? service.findAll() : service.findByManager(managerId);
    }

    @GetMapping("/{id}")
    public TeamResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public TeamResponse update(@PathVariable Long id, @Valid @RequestBody TeamRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}