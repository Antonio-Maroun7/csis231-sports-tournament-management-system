package com.antonio.sportstournament.team;

import com.antonio.sportstournament.exception.ForbiddenException;
import com.antonio.sportstournament.exception.ResourceNotFoundException;
import com.antonio.sportstournament.team.dto.TeamRequest;
import com.antonio.sportstournament.team.dto.TeamResponse;
import com.antonio.sportstournament.user.Role;
import com.antonio.sportstournament.user.User;
import com.antonio.sportstournament.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for teams: createTeam(), getAllTeams(), getTeamById(), updateTeam(), deleteTeam().
 */
@Service
public class TeamService {
    private final TeamRepository repository;
    private final UserService userService;   // the team feature uses the user feature to find the manager
    private final TeamMapper mapper;

    public TeamService(TeamRepository repository, UserService userService, TeamMapper mapper) {
        this.repository = repository;
        this.userService = userService;
        this.mapper = mapper;
    }

    @Transactional
    public TeamResponse create(User caller, TeamRequest request) {
        // Business rule: a team can only be managed by a TEAM_MANAGER (or an ADMIN)
        if (caller.getRole() != Role.TEAM_MANAGER && caller.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only Team Managers can create teams.");
        }
        Team team = mapper.toEntity(request, caller);   // the caller becomes the manager
        return mapper.toResponse(repository.save(team));
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> findAll() {
        return repository.findAllByOrderByTeamNameAsc().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> findByManager(Long managerId) {
        userService.findEntity(managerId);   // 404 if this user does not exist
        return repository.findByManagerIdOrderByTeamNameAsc(managerId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamResponse findById(Long id) {
        return mapper.toResponse(findEntity(id));
    }

    @Transactional
    public TeamResponse update(User caller, Long id, TeamRequest request) {
        Team team = findEntity(id);
        requireManagerOrAdmin(caller, team);
        mapper.updateEntity(team, request);   // the manager never changes
        return mapper.toResponse(repository.save(team));
    }

    /**
     * If the team still has players / venues / registrations, the database foreign key
     * blocks the delete and GlobalExceptionHandler returns 409 CONFLICT.
     */
    @Transactional
    public void delete(User caller, Long id) {
        Team team = findEntity(id);
        requireManagerOrAdmin(caller, team);
        repository.delete(team);
    }

    /**
     * Helpers
     */
    public Team findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found " + id));
    }

    /** Ownership rule shared by the team, player and registration features. */
    public void requireManagerOrAdmin(User caller, Team team) {
        if (caller.getRole() != Role.ADMIN && !team.getManager().getId().equals(caller.getId())) {
            throw new ForbiddenException("Only the team's manager can change this team.");
        }
    }
}