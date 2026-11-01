package com.antonio.sportstournament.registration;

import com.antonio.sportstournament.registration.dto.RegistrationResponse;
import com.antonio.sportstournament.team.Team;
import com.antonio.sportstournament.tournament.Tournament;
import org.springframework.stereotype.Component;

/**
 * Converts between the TournamentTeam entity and its DTOs.
 */
@Component
public class RegistrationMapper {

    public TournamentTeam toEntity(Tournament tournament, Team team) {
        TournamentTeam registration = new TournamentTeam();
        registration.setTournament(tournament);
        registration.setTeam(team);
        registration.setStatus(RegistrationStatus.PENDING);   // rule 3: always starts PENDING
        return registration;
    }

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