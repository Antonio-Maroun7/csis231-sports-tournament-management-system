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

    @Transactional(readOnly = true)
    public List<PlayerResponse> findByTeam(Long teamId) {
        teamService.findEntity(teamId);   // 404 if the team does not exist
        return repository.findByTeamIdOrderByJerseyNumberAsc(teamId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlayerResponse findById(Long id) {
        return mapper.toResponse(findEntity(id));
    }

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