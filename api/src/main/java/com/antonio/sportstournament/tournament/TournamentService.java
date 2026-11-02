package com.antonio.sportstournament.tournament;

import com.antonio.sportstournament.exception.BadRequestException;
import com.antonio.sportstournament.exception.ConflictException;
import com.antonio.sportstournament.exception.ForbiddenException;
import com.antonio.sportstournament.exception.ResourceNotFoundException;
import com.antonio.sportstournament.tournament.dto.TournamentRequest;
import com.antonio.sportstournament.tournament.dto.TournamentResponse;
import com.antonio.sportstournament.user.Role;
import com.antonio.sportstournament.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for tournaments: createTournament(), updateTournament(), deleteTournament(),
 * getAllTournaments(), getTournamentById() and the status lifecycle
 * DRAFT, OPEN, IN_PROGRESS, COMPLETED.
 * <ul>
 *   <li>Rule 1: the end date cannot be before the start date (400).</li>
 *   <li>Rule 2: only an ORGANIZER or an ADMIN can create a tournament; the caller becomes its organizer.</li>
 *   <li>Rule 3: a new tournament always starts as DRAFT.</li>
 *   <li>Rule 4: the status only moves forward, one step at a time (409 otherwise).</li>
 *   <li>Rule 5: only the tournament's organizer (or an ADMIN) can edit, change the status of, or delete it;
 *       a COMPLETED tournament cannot be edited.</li>
 *   <li>Rule 6: a tournament that still has registrations or matches cannot be deleted (409 from the database).</li>
 * </ul>
 */
@Service
public class TournamentService {
    private final TournamentRepository repository;
    private final TournamentMapper mapper;

    public TournamentService(TournamentRepository repository, TournamentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Creates a tournament owned by the caller (Rules 1, 2 and 3).
     *
     * @param caller  the logged-in user, taken from the JWT
     * @param request name and dates
     * @return the saved tournament, with status DRAFT
     * @throws ForbiddenException  if the caller is not an ORGANIZER or ADMIN
     * @throws BadRequestException if the end date is before the start date
     */
    @Transactional
    public TournamentResponse create(User caller, TournamentRequest request) {
        // Rule 2: only an ORGANIZER (or an ADMIN) can own a tournament
        if (caller.getRole() != Role.ORGANIZER && caller.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only Organizers can create tournaments.");
        }
        validateDates(request);
        Tournament tournament = mapper.toEntity(request, caller);   // the caller becomes the organizer
        return mapper.toResponse(repository.save(tournament));
    }

    /**
     * All tournaments, or only one status (?status=OPEN), or only one organizer's (?organizerId=2).
     * If both filters are given, the status filter is used.
     *
     * @param status      optional status filter
     * @param organizerId optional organizer filter
     * @return the matching tournaments as DTOs
     */
    @Transactional(readOnly = true)
    public List<TournamentResponse> findAll(TournamentStatus status, Long organizerId) {
        List<Tournament> tournaments;
        if (status != null) {
            tournaments = repository.findByStatusOrderByStartDateAsc(status);
        } else if (organizerId != null) {
            tournaments = repository.findByOrganizerIdOrderByStartDateDesc(organizerId);
        } else {
            tournaments = repository.findAllByOrderByStartDateDesc();
        }
        return tournaments.stream().map(mapper::toResponse).toList();
    }

    /**
     * Finds one tournament.
     *
     * @param id the tournament id
     * @return the tournament as a DTO
     * @throws ResourceNotFoundException if it does not exist
     */
    @Transactional(readOnly = true)
    public TournamentResponse findById(Long id) {
        return mapper.toResponse(findEntity(id));
    }

    /**
     * Edits the name and dates of a tournament (Rules 1 and 5). The status is not changed here.
     *
     * @param caller  the logged-in user
     * @param id      the tournament id
     * @param request the new name and dates
     * @return the updated tournament
     * @throws ResourceNotFoundException if it does not exist
     * @throws ForbiddenException        if the caller is neither its organizer nor an ADMIN
     * @throws ConflictException         if the tournament is COMPLETED
     * @throws BadRequestException       if the end date is before the start date
     */
    @Transactional
    public TournamentResponse update(User caller, Long id, TournamentRequest request) {
        Tournament tournament = findEntity(id);
        requireOrganizerOrAdmin(caller, tournament);
        if (tournament.getStatus() == TournamentStatus.COMPLETED) {
            throw new ConflictException("A completed tournament cannot be edited");
        }
        validateDates(request);
        mapper.updateEntity(tournament, request);
        return mapper.toResponse(repository.save(tournament));
    }

    /**
     * Rule 4: the status only moves FORWARD, one step at a time:
     * DRAFT, then OPEN, then IN_PROGRESS, then COMPLETED. Skipping or going back is refused.
     *
     * @param caller    the logged-in user
     * @param id        the tournament id
     * @param newStatus the requested status (must be the one right after the current status)
     * @return the updated tournament
     * @throws ResourceNotFoundException if it does not exist
     * @throws ForbiddenException        if the caller is neither its organizer nor an ADMIN
     * @throws ConflictException         if newStatus is not the next status
     */
    @Transactional
    public TournamentResponse changeStatus(User caller, Long id, TournamentStatus newStatus) {
        Tournament tournament = findEntity(id);
        requireOrganizerOrAdmin(caller, tournament);
        TournamentStatus current = tournament.getStatus();
        if (newStatus.ordinal() != current.ordinal() + 1) {
            throw new ConflictException("Cannot change status from " + current + " to " + newStatus
                    + (current == TournamentStatus.COMPLETED
                    ? " (the tournament is already completed)"
                    : " (next allowed status: " + TournamentStatus.values()[current.ordinal() + 1] + ")"));
        }
        tournament.setStatus(newStatus);
        return mapper.toResponse(repository.save(tournament));
    }

    /**
     * Deletes a tournament (Rule 5). Rule 6: if the tournament already has registrations or matches,
     * the database foreign keys block the delete and GlobalExceptionHandler returns 409 CONFLICT.
     *
     * @param caller the logged-in user
     * @param id     the tournament id
     * @throws ResourceNotFoundException if it does not exist
     * @throws ForbiddenException        if the caller is neither its organizer nor an ADMIN
     */
    @Transactional
    public void delete(User caller, Long id) {
        Tournament tournament = findEntity(id);
        requireOrganizerOrAdmin(caller, tournament);
        repository.delete(tournament);
    }

    /**
     * Helpers
     */
    /**
     * Loads the tournament entity.
     *
     * @param id the tournament id
     * @return the Tournament entity
     * @throws ResourceNotFoundException if it does not exist
     */
    public Tournament findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found " + id));
    }

    /**
     * Ownership rule shared by the tournament and registration features.
     *
     * @param caller     the logged-in user
     * @param tournament the tournament being changed
     * @throws ForbiddenException if the caller is neither the tournament's organizer nor an ADMIN
     */
    public void requireOrganizerOrAdmin(User caller, Tournament tournament) {
        if (caller.getRole() != Role.ADMIN && !tournament.getOrganizer().getId().equals(caller.getId())) {
            throw new ForbiddenException("Only the tournament's organizer can change this tournament.");
        }
    }

    // Rule 1: the end date cannot be before the start date
    private void validateDates(TournamentRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new BadRequestException("End date must be on or after the start date");
        }
    }
}