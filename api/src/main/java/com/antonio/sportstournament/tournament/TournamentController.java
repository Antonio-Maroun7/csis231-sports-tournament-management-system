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
 * <ul>
 *   <li>POST   /api/tournaments              - create a tournament (ORGANIZER or ADMIN), 201</li>
 *   <li>GET    /api/tournaments              - list tournaments, optional {@code ?status=} or {@code ?organizerId=}</li>
 *   <li>GET    /api/tournaments/{id}         - one tournament</li>
 *   <li>PUT    /api/tournaments/{id}         - edit name and dates (its organizer or ADMIN)</li>
 *   <li>PUT    /api/tournaments/{id}/status  - move to the next status (its organizer or ADMIN)</li>
 *   <li>DELETE /api/tournaments/{id}         - delete (its organizer or ADMIN), 204</li>
 * </ul>
 * Every endpoint needs a valid JWT (401 otherwise). Ownership and status rules are in {@link TournamentService}.
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

    /**
     * {@code POST /api/tournaments} - creates a tournament owned by the caller. Allowed roles: ORGANIZER, ADMIN
     * (a TEAM_MANAGER gets 403). The organizer comes from the JWT, never from the body; the status starts as DRAFT.
     * Status 201; 400 invalid body or end date before start date.
     *
     * @param request        name, start date and end date
     * @param authentication the logged-in caller
     * @return the new tournament
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TournamentResponse create(@Valid @RequestBody TournamentRequest request,
                                     Authentication authentication) {
        return service.create(currentUserService.require(authentication), request);
    }

    /**
     * {@code GET /api/tournaments} - any logged-in user. Filters (the status filter wins if both are sent):
     * <pre>
     * GET /api/tournaments                 : all, newest start date first
     * GET /api/tournaments?status=OPEN     : open for registration
     * GET /api/tournaments?organizerId=2   : one organizer's tournaments
     * </pre>
     * Status 200.
     *
     * @param status      optional status filter
     * @param organizerId optional organizer filter
     * @return the matching tournaments
     */
    @GetMapping
    public List<TournamentResponse> findAll(@RequestParam(required = false) TournamentStatus status,
                                            @RequestParam(required = false) Long organizerId) {
        return service.findAll(status, organizerId);
    }

    /**
     * {@code GET /api/tournaments/{id}} - any logged-in user. Status 200, or 404 if it does not exist.
     *
     * @param id the tournament id
     * @return the tournament
     */
    @GetMapping("/{id}")
    public TournamentResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    /**
     * {@code PUT /api/tournaments/{id}} - edits the name and dates. Only the tournament's organizer or an ADMIN.
     * The status is not changed here. Status 200; 400 invalid body or end before start, 403 not the owner,
     * 404 unknown tournament, 409 tournament is COMPLETED.
     *
     * @param id             the tournament id
     * @param request        the new name and dates
     * @param authentication the logged-in caller
     * @return the updated tournament
     */
    @PutMapping("/{id}")
    public TournamentResponse update(@PathVariable Long id, @Valid @RequestBody TournamentRequest request,
                                     Authentication authentication) {
        return service.update(currentUserService.require(authentication), id, request);
    }

    /**
     * {@code PUT /api/tournaments/{id}/status} - moves the tournament to its next status. Only the
     * tournament's organizer or an ADMIN. Status 200; 403 not the owner, 404 unknown tournament,
     * 409 if the requested status is not the next one.
     *
     * @param id             the tournament id
     * @param request        the new status
     * @param authentication the logged-in caller
     * @return the updated tournament
     */
    @PutMapping("/{id}/status")
    public TournamentResponse changeStatus(@PathVariable Long id,
                                           @Valid @RequestBody TournamentStatusRequest request,
                                           Authentication authentication) {
        return service.changeStatus(currentUserService.require(authentication), id, request.status());
    }

    /**
     * {@code DELETE /api/tournaments/{id}} - deletes a tournament. Only the tournament's organizer or an ADMIN.
     * Status 204; 403 not the owner, 404 unknown tournament, 409 if it still has registrations or matches.
     *
     * @param id             the tournament id
     * @param authentication the logged-in caller
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.delete(currentUserService.require(authentication), id);
    }
}