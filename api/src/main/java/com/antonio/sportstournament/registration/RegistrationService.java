package com.antonio.sportstournament.registration;

import com.antonio.sportstournament.exception.ConflictException;
import com.antonio.sportstournament.exception.ResourceNotFoundException;
import com.antonio.sportstournament.registration.dto.RegistrationResponse;
import com.antonio.sportstournament.team.Team;
import com.antonio.sportstournament.team.TeamService;
import com.antonio.sportstournament.tournament.Tournament;
import com.antonio.sportstournament.tournament.TournamentService;
import com.antonio.sportstournament.tournament.TournamentStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for team registrations (tournament_teams):
 * registerTeamInTournament(), approveTeamRegistration(), rejectTeamRegistration(),
 * removeTeamFromTournament(), getTournamentTeams().
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

    @Transactional(readOnly = true)
    public List<RegistrationResponse> findByTournament(Long tournamentId) {
        tournamentService.findEntity(tournamentId);   // 404 if the tournament does not exist
        return repository.findByTournamentIdOrderByRegisteredAtAsc(tournamentId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> findByTeam(Long teamId) {
        teamService.findEntity(teamId);               // 404 if the team does not exist
        return repository.findByTeamIdOrderByRegisteredAtDesc(teamId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public RegistrationResponse register(Long tournamentId, Long teamId) {
        Tournament tournament = tournamentService.findEntity(tournamentId);
        Team team = teamService.findEntity(teamId);

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

    /** Rule 3: the Organizer approves a PENDING registration. */
    @Transactional
    public RegistrationResponse approve(Long id) {
        return decide(id, RegistrationStatus.APPROVED);
    }

    /** Rule 3: the Organizer rejects a PENDING registration. */
    @Transactional
    public RegistrationResponse reject(Long id) {
        return decide(id, RegistrationStatus.REJECTED);
    }

    /** Rule 4: a team can withdraw only while registration is still open. */
    @Transactional
    public void withdraw(Long id) {
        TournamentTeam registration = findEntity(id);
        if (registration.getTournament().getStatus() != TournamentStatus.OPEN) {
            throw new ConflictException("A team can only withdraw while registration is open");
        }
        repository.delete(registration);
    }

    /**
     * Helpers
     */
    private RegistrationResponse decide(Long id, RegistrationStatus decision) {
        TournamentTeam registration = findEntity(id);
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

    public TournamentTeam findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found " + id));
    }
}