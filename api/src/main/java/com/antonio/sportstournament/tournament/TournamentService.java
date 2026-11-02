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
 * DRAFT -> OPEN -> IN_PROGRESS -> COMPLETED.
 */
@Service
public class TournamentService {
    private final TournamentRepository repository;
    private final TournamentMapper mapper;

    public TournamentService(TournamentRepository repository, TournamentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

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

    /** All tournaments, or only one status (?status=OPEN), or only one organizer's (?organizerId=2). */
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

    @Transactional(readOnly = true)
    public TournamentResponse findById(Long id) {
        return mapper.toResponse(findEntity(id));
    }

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
     * DRAFT -> OPEN -> IN_PROGRESS -> COMPLETED.
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
     * Rule 6: if the tournament already has registrations or matches, the database foreign keys
     * block the delete and GlobalExceptionHandler returns 409 CONFLICT.
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
    public Tournament findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament not found " + id));
    }

    /** Ownership rule shared by the tournament and registration features. */
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