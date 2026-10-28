package com.antonio.sportstournament.team;

import com.antonio.sportstournament.team.dto.TeamRequest;
import com.antonio.sportstournament.team.dto.TeamResponse;
import com.antonio.sportstournament.user.User;
import org.springframework.stereotype.Component;

/**
 * Converts between the Team entity and its DTOs (same role as the doctor's StudentMapper).
 */
@Component
public class TeamMapper {

    public Team toEntity(TeamRequest request, User manager) {
        Team team = new Team();
        team.setTeamName(request.teamName().trim());
        team.setManager(manager);
        return team;
    }

    public void updateEntity(Team team, TeamRequest request, User manager) {
        team.setTeamName(request.teamName().trim());
        team.setManager(manager);
    }

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