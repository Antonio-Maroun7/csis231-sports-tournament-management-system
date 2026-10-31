package com.antonio.sportstournament.tournament;

import com.antonio.sportstournament.tournament.dto.TournamentRequest;
import com.antonio.sportstournament.tournament.dto.TournamentResponse;
import com.antonio.sportstournament.user.User;
import org.springframework.stereotype.Component;

/**
 * Converts between the Tournament entity and its DTOs.
 */
@Component
public class TournamentMapper {

    public Tournament toEntity(TournamentRequest request, User organizer) {
        Tournament tournament = new Tournament();
        tournament.setStatus(TournamentStatus.DRAFT);   // rule 3: always starts as DRAFT
        tournament.setOrganizer(organizer);
        updateEntity(tournament, request);
        return tournament;
    }

    public void updateEntity(Tournament tournament, TournamentRequest request) {
        tournament.setName(request.name().trim());
        tournament.setStartDate(request.startDate());
        tournament.setEndDate(request.endDate());
    }

    public TournamentResponse toResponse(Tournament tournament) {
        User organizer = tournament.getOrganizer();
        return new TournamentResponse(
                tournament.getId(),
                tournament.getName(),
                organizer.getId(),
                organizer.getName(),
                tournament.getStartDate(),
                tournament.getEndDate(),
                tournament.getStatus(),
                tournament.getCreatedAt()
        );
    }
}