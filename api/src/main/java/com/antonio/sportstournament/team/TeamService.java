package com.antonio.sportstournament.team;

import com.antonio.sportstournament.exception.BadRequestException;
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
    public TeamResponse create(TeamRequest request) {
        User manager = findManager(request.managerId());
        Team team = mapper.toEntity(request, manager);
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
    public TeamResponse update(Long id, TeamRequest request) {
        Team team = findEntity(id);
        User manager = findManager(request.managerId());
        mapper.updateEntity(team, request, manager);
        return mapper.toResponse(repository.save(team));
    }

    /**
     * If the team still has players / venues / registrations, the database foreign key
     * blocks the delete and GlobalExceptionHandler returns 409 CONFLICT.
     */
    @Transactional
    public void delete(Long id) {
        Team team = findEntity(id);
        repository.delete(team);
    }

    /**
     * Helpers
     */
    public Team findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found " + id));
    }

    // Business rule: a team can only be managed by a TEAM_MANAGER (or an ADMIN)
    private User findManager(Long managerId) {
        User manager = userService.findEntity(managerId);   // 404 if not found
        if (manager.getRole() != Role.TEAM_MANAGER && manager.getRole() != Role.ADMIN) {
            throw new BadRequestException("User " + managerId + " is not a Team Manager");
        }
        return manager;
    }
}