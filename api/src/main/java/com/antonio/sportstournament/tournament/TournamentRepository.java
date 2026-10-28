package com.antonio.sportstournament.tournament;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * Data access for the tournaments table.
 */
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    // Tournaments View: newest first
    //the tournament with the latest start date comes first
    List<Tournament> findAllByOrderByStartDateDesc();

    // ?status=OPEN -> tournaments a team can register for (Step 7)
    //Find tournaments with the given status
    // Sort them by startDate in ascending order
    List<Tournament> findByStatusOrderByStartDateAsc(TournamentStatus status);

    // ?organizerId=2 -> "my tournaments" of an Organizer
    //find all tournaments created/organized by that organizerId
    //sort them by startDate
    //Desc means latest start date first, earliest start date last
    List<Tournament> findByOrganizerIdOrderByStartDateDesc(Long organizerId);

    // Dashboard card "N active" = OPEN or IN_PROGRESS
    //Count tournaments with the given statuses
    long countByStatusIn(Collection<TournamentStatus> statuses);
}