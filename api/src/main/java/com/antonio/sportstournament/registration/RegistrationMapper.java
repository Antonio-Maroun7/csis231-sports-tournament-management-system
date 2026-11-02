package com.antonio.sportstournament.registration;

import com.antonio.sportstournament.registration.dto.RegistrationResponse;
import com.antonio.sportstournament.team.Team;
import com.antonio.sportstournament.tournament.Tournament;
import org.springframework.stereotype.Component;

/**
 * Converts between the TournamentTeam entity and its DTOs.
 * Creates a PENDING registration from a tournament and a team, and converts a registration to {@link RegistrationResponse}.
 */
@Component
public class RegistrationMapper {

    /**
     * Builds a new registration linking a team to a tournament. The status always starts PENDING.
     *
     * @param tournament the tournament being joined
     * @param team       the registering team
     * @return an unsaved TournamentTeam
     */
    public TournamentTeam toEntity(Tournament tournament, Team team) {
        TournamentTeam registration = new TournamentTeam();
        registration.setTournament(tournament);
        registration.setTeam(team);
        registration.setStatus(RegistrationStatus.PENDING);   // rule 3: always starts PENDING
        return registration;
    }

    /**
     * Converts a registration to the JSON sent to clients: ids and names of the tournament and the team,
     * the tournament status, and the team's manager (so the client knows who may withdraw).
     *
     * @param registration the entity (its tournament, team and the team's manager must be loadable)
     * @return the response DTO
     */
    public RegistrationResponse toResponse(TournamentTeam registration) {
        Tournament tournament = registration.getTournament();
        Team team = registration.getTeam();
        return new RegistrationResponse(
                registration.getId(),
                tournament.getId(),
                tournament.getName(),
                tournament.getStatus(),
                team.getId(),
                team.getTeamName(),
                team.getManager().getId(),
                team.getManager().getName(),
                registration.getStatus(),
                registration.getRegisteredAt()
        );
    }
}