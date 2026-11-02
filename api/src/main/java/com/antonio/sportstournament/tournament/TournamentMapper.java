package com.antonio.sportstournament.tournament;

import com.antonio.sportstournament.tournament.dto.TournamentRequest;
import com.antonio.sportstournament.tournament.dto.TournamentResponse;
import com.antonio.sportstournament.user.User;
import org.springframework.stereotype.Component;

/**
 * Converts between the Tournament entity and its DTOs.
 * {@link TournamentRequest} to {@link Tournament} on input, {@link Tournament} to {@link TournamentResponse} on output.
 */
@Component
public class TournamentMapper {

    /**
     * Builds a new Tournament from a create request. The status always starts as DRAFT.
     *
     * @param request   the validated request
     * @param organizer the logged-in caller, who becomes the organizer
     * @return an unsaved Tournament
     */
    public Tournament toEntity(TournamentRequest request, User organizer) {
        Tournament tournament = new Tournament();
        tournament.setStatus(TournamentStatus.DRAFT);   // rule 3: always starts as DRAFT
        tournament.setOrganizer(organizer);
        updateEntity(tournament, request);
        return tournament;
    }

    /**
     * Copies the editable fields (name trimmed, start date, end date) onto a tournament.
     * The status and the organizer are not changed.
     *
     * @param tournament the entity to update
     * @param request    the validated request
     */
    public void updateEntity(Tournament tournament, TournamentRequest request) {
        tournament.setName(request.name().trim());
        tournament.setStartDate(request.startDate());
        tournament.setEndDate(request.endDate());
    }

    /**
     * Converts a tournament to the JSON sent to clients, flattening the organizer into
     * {@code organizerId} and {@code organizerName}.
     *
     * @param tournament the entity (its organizer must be loadable)
     * @return the response DTO
     */
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