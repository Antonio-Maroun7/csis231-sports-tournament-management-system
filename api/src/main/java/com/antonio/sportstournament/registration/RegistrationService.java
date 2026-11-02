package com.antonio.sportstournament.registration;

import com.antonio.sportstournament.exception.ConflictException;
import com.antonio.sportstournament.exception.ForbiddenException;
import com.antonio.sportstournament.exception.ResourceNotFoundException;
import com.antonio.sportstournament.registration.dto.RegistrationResponse;
import com.antonio.sportstournament.team.Team;
import com.antonio.sportstournament.team.TeamService;
import com.antonio.sportstournament.tournament.Tournament;
import com.antonio.sportstournament.tournament.TournamentService;
import com.antonio.sportstournament.tournament.TournamentStatus;
import com.antonio.sportstournament.user.Role;
import com.antonio.sportstournament.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for team registrations (tournament_teams):
 * registerTeamInTournament(), approveTeamRegistration(), rejectTeamRegistration(),
 * removeTeamFromTournament(), getTournamentTeams().
 * <ul>
 *   <li>Rule 1: a team can register only while the tournament is OPEN.</li>
 *   <li>Rule 2: a team cannot register twice in the same tournament.</li>
 *   <li>Rule 3: a new registration is PENDING; the tournament's organizer approves or rejects it.</li>
 *   <li>Rule 4: a team can withdraw only while the tournament is OPEN.</li>
 *   <li>Teams are registered and withdrawn by their own manager (or an ADMIN).</li>
 * </ul>
 */
@Service
public class RegistrationService {
    private final RegistrationRepository repository;
    private final TournamentService tournamentService;   // registration uses the tournament feature
    private final TeamService teamService;               // ... and the team feature
    private final RegistrationMapper mapper;

    public RegistrationService(RegistrationRepository repository,
                               TournamentService tournamentService,
                               TeamService teamService,
                               RegistrationMapper mapper) {
        this.repository = repository;
        this.tournamentService = tournamentService;
        this.teamService = teamService;
        this.mapper = mapper;
    }

    /**
     * Lists the teams registered in a tournament.
     *
     * @param tournamentId the tournament id
     * @return its registrations, oldest first
     * @throws ResourceNotFoundException if the tournament does not exist
     */
    @Transactional(readOnly = true)
    public List<RegistrationResponse> findByTournament(Long tournamentId) {
        tournamentService.findEntity(tournamentId);   // 404 if the tournament does not exist
        return repository.findByTournamentIdOrderByRegisteredAtAsc(tournamentId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Lists the tournaments a team has registered for.
     *
     * @param teamId the team id
     * @return the team's registrations, newest first
     * @throws ResourceNotFoundException if the team does not exist
     */
    @Transactional(readOnly = true)
    public List<RegistrationResponse> findByTeam(Long teamId) {
        teamService.findEntity(teamId);               // 404 if the team does not exist
        return repository.findByTeamIdOrderByRegisteredAtDesc(teamId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Registers a team in a tournament. The team must belong to the caller unless the caller is an ADMIN,
     * then Rules 1 and 2 are checked. The registration starts as PENDING (Rule 3).
     *
     * @param caller       the logged-in user
     * @param tournamentId the tournament to join
     * @param teamId       the team to register
     * @return the new PENDING registration
     * @throws ResourceNotFoundException if the tournament or the team does not exist
     * @throws ForbiddenException        if the team is not managed by the caller (and the caller is not an ADMIN)
     * @throws ConflictException         if the tournament is not OPEN, or the team is already registered
     */
    @Transactional
    public RegistrationResponse register(User caller, Long tournamentId, Long teamId) {
        Tournament tournament = tournamentService.findEntity(tournamentId);
        Team team = teamService.findEntity(teamId);

        if (caller.getRole() != Role.ADMIN && !team.getManager().getId().equals(caller.getId())) {
            throw new ForbiddenException("You can only register your own teams.");
        }
        // Rule 1: registration only while the tournament is OPEN
        if (tournament.getStatus() != TournamentStatus.OPEN) {
            throw new ConflictException("'" + tournament.getName() + "' is not open for registration (status: "
                    + tournament.getStatus() + ")");
        }
        // Rule 2: no double registration
        if (repository.existsByTournamentIdAndTeamId(tournamentId, teamId)) {
            throw new ConflictException("'" + team.getTeamName() + "' is already registered in '"
                    + tournament.getName() + "'");
        }
        TournamentTeam registration = mapper.toEntity(tournament, team);
        return mapper.toResponse(repository.save(registration));
    }

    /**
     * Rule 3: the Organizer approves a PENDING registration.
     *
     * @param caller the logged-in user (the tournament's organizer or an ADMIN)
     * @param id     the registration id
     * @return the registration, now APPROVED
     * @throws ResourceNotFoundException if the registration does not exist
     * @throws ForbiddenException        if the caller does not own the tournament
     * @throws ConflictException         if the registration is not PENDING or the tournament is COMPLETED
     */
    @Transactional
    public RegistrationResponse approve(User caller, Long id) {
        return decide(caller, id, RegistrationStatus.APPROVED);
    }

    /**
     * Rule 3: the Organizer rejects a PENDING registration.
     *
     * @param caller the logged-in user (the tournament's organizer or an ADMIN)
     * @param id     the registration id
     * @return the registration, now REJECTED
     * @throws ResourceNotFoundException if the registration does not exist
     * @throws ForbiddenException        if the caller does not own the tournament
     * @throws ConflictException         if the registration is not PENDING or the tournament is COMPLETED
     */
    @Transactional
    public RegistrationResponse reject(User caller, Long id) {
        return decide(caller, id, RegistrationStatus.REJECTED);
    }

    /**
     * Rule 4: a team can withdraw only while registration is still open.
     *
     * @param caller the logged-in user (the team's manager or an ADMIN)
     * @param id     the registration id
     * @throws ResourceNotFoundException if the registration does not exist
     * @throws ForbiddenException        if the caller does not manage the team
     * @throws ConflictException         if the tournament is no longer OPEN
     */
    @Transactional
    public void withdraw(User caller, Long id) {
        TournamentTeam registration = findEntity(id);
        teamService.requireManagerOrAdmin(caller, registration.getTeam());
        if (registration.getTournament().getStatus() != TournamentStatus.OPEN) {
            throw new ConflictException("A team can only withdraw while registration is open");
        }
        repository.delete(registration);
    }

    /**
     * Helpers
     */
    private RegistrationResponse decide(User caller, Long id, RegistrationStatus decision) {
        TournamentTeam registration = findEntity(id);
        tournamentService.requireOrganizerOrAdmin(caller, registration.getTournament());
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new ConflictException("Only a PENDING registration can be " + decision.name().toLowerCase()
                    + " (this one is " + registration.getStatus() + ")");
        }
        if (registration.getTournament().getStatus() == TournamentStatus.COMPLETED) {
            throw new ConflictException("The tournament is already completed");
        }
        registration.setStatus(decision);
        return mapper.toResponse(repository.save(registration));
    }

    /**
     * Loads the registration entity.
     *
     * @param id the registration id
     * @return the TournamentTeam entity
     * @throws ResourceNotFoundException if it does not exist
     */
    public TournamentTeam findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found " + id));
    }
}