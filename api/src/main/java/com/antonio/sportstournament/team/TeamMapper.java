package com.antonio.sportstournament.team;

import com.antonio.sportstournament.team.dto.TeamRequest;
import com.antonio.sportstournament.team.dto.TeamResponse;
import com.antonio.sportstournament.user.User;
import org.springframework.stereotype.Component;

/**
 * Converts between the Team entity and its DTOs (same role as the doctor's StudentMapper).
 * {@link TeamRequest} to {@link Team} on input, {@link Team} to {@link TeamResponse} on output.
 */
@Component
public class TeamMapper {

    /**
     * Builds a new Team from a create request.
     *
     * @param request the validated request (the name is trimmed)
     * @param manager the logged-in caller, who becomes the team's manager
     * @return an unsaved Team
     */
    public Team toEntity(TeamRequest request, User manager) {
        Team team = new Team();
        team.setTeamName(request.teamName().trim());
        team.setManager(manager);
        return team;
    }

    /**
     * Copies the editable field (only the name) from the request onto an existing team; the manager is not changed.
     *
     * @param team    the entity to update
     * @param request the validated request (the name is trimmed)
     */
    public void updateEntity(Team team, TeamRequest request) {
        team.setTeamName(request.teamName().trim());
    }

    /**
     * Converts a team to the JSON shape sent to clients, flattening the manager into
     * {@code managerId} and {@code managerName}.
     *
     * @param team the entity (its manager must be loadable)
     * @return the response DTO
     */
    public TeamResponse toResponse(Team team) {
        User manager = team.getManager();
        return new TeamResponse(
                team.getId(),
                team.getTeamName(),
                manager.getId(),
                manager.getName(),
                team.getCreatedAt()
        );
    }
}