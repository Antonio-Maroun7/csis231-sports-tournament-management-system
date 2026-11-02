package com.antonio.sportstournament.player;

import com.antonio.sportstournament.exception.ConflictException;
import com.antonio.sportstournament.exception.ResourceNotFoundException;
import com.antonio.sportstournament.player.dto.PlayerRequest;
import com.antonio.sportstournament.player.dto.PlayerResponse;
import com.antonio.sportstournament.team.Team;
import com.antonio.sportstournament.team.TeamService;
import com.antonio.sportstournament.user.User;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for players: addPlayer(), updatePlayer(), removePlayer(), getPlayersByTeam().
 * <ul>
 *   <li>Rule 1: only the manager of the team (or an ADMIN) can add, edit or remove its players.</li>
 *   <li>Rule 2: a jersey number is unique inside one team (409 if already taken).</li>
 *   <li>Reading a roster or a player is allowed for any logged-in user.</li>
 * </ul>
 */
@Service
public class PlayerService {
    private final PlayerRepository repository;
    private final TeamService teamService;   // the player feature uses the team feature
    private final PlayerMapper mapper;

    public PlayerService(PlayerRepository repository, TeamService teamService, PlayerMapper mapper) {
        this.repository = repository;
        this.teamService = teamService;
        this.mapper = mapper;
    }

    /**
     * Lists the roster of a team.
     *
     * @param teamId the team id
     * @return the team's players ordered by jersey number
     * @throws ResourceNotFoundException if the team does not exist
     */
    @Transactional(readOnly = true)
    public List<PlayerResponse> findByTeam(Long teamId) {
        teamService.findEntity(teamId);   // 404 if the team does not exist
        return repository.findByTeamIdOrderByJerseyNumberAsc(teamId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Finds one player.
     *
     * @param id the player id
     * @return the player as a DTO
     * @throws ResourceNotFoundException if the player does not exist
     */
    @Transactional(readOnly = true)
    public PlayerResponse findById(Long id) {
        return mapper.toResponse(findEntity(id));
    }

    /**
     * Adds a player to a team (Rules 1 and 2).
     *
     * @param caller  the logged-in user
     * @param teamId  the team that gets the player
     * @param request the player data
     * @return the saved player
     * @throws ResourceNotFoundException if the team does not exist
     * @throws com.antonio.sportstournament.exception.ForbiddenException if the caller is not the team's manager or an ADMIN
     * @throws ConflictException         if the jersey number is already used in this team
     */
    @Transactional
    public PlayerResponse add(User caller, Long teamId, PlayerRequest request) {
        Team team = teamService.findEntity(teamId);
        teamService.requireManagerOrAdmin(caller, team);
        if (repository.existsByTeamIdAndJerseyNumber(teamId, request.jerseyNumber())) {
            throw new ConflictException("Jersey number " + request.jerseyNumber()
                    + " is already taken on " + team.getTeamName());
        }
        Player player = mapper.toEntity(request, team);
        return mapper.toResponse(repository.save(player));
    }

    /**
     * Edits a player (Rules 1 and 2). The player stays in the same team; the jersey-number check ignores
     * the player being edited, so keeping the same number is allowed.
     *
     * @param caller  the logged-in user
     * @param id      the player id
     * @param request the new player data
     * @return the updated player
     * @throws ResourceNotFoundException if the player does not exist
     * @throws com.antonio.sportstournament.exception.ForbiddenException if the caller is not the team's manager or an ADMIN
     * @throws ConflictException         if another player of the team already has this jersey number
     */
    @Transactional
    public PlayerResponse update(User caller, Long id, PlayerRequest request) {
        Player player = findEntity(id);
        Team team = player.getTeam();
        teamService.requireManagerOrAdmin(caller, team);
        if (repository.existsByTeamIdAndJerseyNumberAndIdNot(team.getId(), request.jerseyNumber(), id)) {
            throw new ConflictException("Jersey number " + request.jerseyNumber()
                    + " is already taken on " + team.getTeamName());
        }
        mapper.updateEntity(player, request);
        return mapper.toResponse(repository.save(player));
    }

    /**
     * Removes a player from its team (Rule 1).
     *
     * @param caller the logged-in user
     * @param id     the player id
     * @throws ResourceNotFoundException if the player does not exist
     * @throws com.antonio.sportstournament.exception.ForbiddenException if the caller is not the team's manager or an ADMIN
     */
    @Transactional
    public void remove(User caller, Long id) {
        Player player = findEntity(id);
        teamService.requireManagerOrAdmin(caller, player.getTeam());
        repository.delete(player);
    }

    /**
     * Helpers
     */
    private Player findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found " + id));
    }
}