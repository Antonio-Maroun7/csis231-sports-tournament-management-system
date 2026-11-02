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
 * <ul>
 *   <li>Rule 1: only a TEAM_MANAGER or an ADMIN can create a team; the caller becomes its manager.</li>
 *   <li>Rule 2: only the team's manager (or an ADMIN) can rename or delete it.</li>
 *   <li>Rule 3: a team that still has players or registrations cannot be deleted (409 from the database).</li>
 * </ul>
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

    /**
     * Creates a team managed by the caller (Rule 1).
     *
     * @param caller  the logged-in user, taken from the JWT
     * @param request the team name
     * @return the saved team
     * @throws ForbiddenException if the caller is not a TEAM_MANAGER or ADMIN (for example an ORGANIZER)
     */
    @Transactional
    public TeamResponse create(User caller, TeamRequest request) {
        // Business rule: a team can only be managed by a TEAM_MANAGER (or an ADMIN)
        if (caller.getRole() != Role.TEAM_MANAGER && caller.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only Team Managers can create teams.");
        }
        Team team = mapper.toEntity(request, caller);   // the caller becomes the manager
        return mapper.toResponse(repository.save(team));
    }

    /**
     * Lists every team, ordered by name.
     *
     * @return all teams as DTOs
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> findAll() {
        return repository.findAllByOrderByTeamNameAsc().stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Lists the teams of one manager, ordered by name.
     *
     * @param managerId the user id of the manager
     * @return that manager's teams as DTOs (empty if there are none)
     * @throws ResourceNotFoundException if no user has this id
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> findByManager(Long managerId) {
        userService.findEntity(managerId);   // 404 if this user does not exist
        return repository.findByManagerIdOrderByTeamNameAsc(managerId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Finds one team.
     *
     * @param id the team id
     * @return the team as a DTO
     * @throws ResourceNotFoundException if the team does not exist
     */
    @Transactional(readOnly = true)
    public TeamResponse findById(Long id) {
        return mapper.toResponse(findEntity(id));
    }

    /**
     * Renames a team (Rule 2). The manager of the team never changes.
     *
     * @param caller  the logged-in user
     * @param id      the team id
     * @param request the new name
     * @return the updated team
     * @throws ResourceNotFoundException if the team does not exist
     * @throws ForbiddenException        if the caller is neither the team's manager nor an ADMIN
     */
    @Transactional
    public TeamResponse update(User caller, Long id, TeamRequest request) {
        Team team = findEntity(id);
        requireManagerOrAdmin(caller, team);
        mapper.updateEntity(team, request);   // the manager never changes
        return mapper.toResponse(repository.save(team));
    }

    /**
     * Deletes a team (Rule 2). If the team still has players / venues / registrations, the database foreign key
     * blocks the delete and GlobalExceptionHandler returns 409 CONFLICT (Rule 3).
     *
     * @param caller the logged-in user
     * @param id     the team id
     * @throws ResourceNotFoundException if the team does not exist
     * @throws ForbiddenException        if the caller is neither the team's manager nor an ADMIN
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
    /**
     * Loads the team entity.
     *
     * @param id the team id
     * @return the Team entity
     * @throws ResourceNotFoundException if the team does not exist
     */
    public Team findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found " + id));
    }

    /**
     * Ownership rule shared by the team, player and registration features.
     *
     * @param caller the logged-in user
     * @param team   the team being changed
     * @throws ForbiddenException if the caller is neither the team's manager nor an ADMIN
     */
    public void requireManagerOrAdmin(User caller, Team team) {
        if (caller.getRole() != Role.ADMIN && !team.getManager().getId().equals(caller.getId())) {
            throw new ForbiddenException("Only the team's manager can change this team.");
        }
    }
}