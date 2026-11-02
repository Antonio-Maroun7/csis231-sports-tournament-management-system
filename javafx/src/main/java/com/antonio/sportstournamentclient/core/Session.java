package com.antonio.sportstournamentclient.core;

import com.antonio.sportstournamentclient.auth.dto.AuthResult;
import com.antonio.sportstournamentclient.auth.dto.LoginResponse;

/**
 * Who is logged in, plus small values passed between screens (same role as the instructor's Session).
 */
public class Session {
    private static LoginResponse currentUser;

    // login waiting for its emailed 2FA code (set by the Login view, used by the 2FA view)
    private static AuthResult pendingChallenge;

    // team chosen in the Teams view, shown by the Team Details view
    private static Long selectedTeamId;

    // tournament chosen in the Tournaments view, shown by the Tournament Details view
    private static Long selectedTournamentId;

    public static void setCurrentUser(LoginResponse user) {
        currentUser = user;
    }

    public static LoginResponse getCurrentUser() {
        return currentUser;
    }

    public static AuthResult getPendingChallenge() {
        return pendingChallenge;
    }

    public static void setPendingChallenge(AuthResult challenge) {
        pendingChallenge = challenge;
    }

    public static Long getSelectedTeamId() {
        return selectedTeamId;
    }

    public static void setSelectedTeamId(Long teamId) {
        selectedTeamId = teamId;
    }

    public static Long getSelectedTournamentId() {
        return selectedTournamentId;
    }

    public static void setSelectedTournamentId(Long tournamentId) {
        selectedTournamentId = tournamentId;
    }

    public static void clear() {
        currentUser = null;
        pendingChallenge = null;
        selectedTeamId = null;
        selectedTournamentId = null;
    }

    // -------- role helpers (the server still enforces its own rules) --------

    public static boolean hasRole(String role) {
        return currentUser != null && role.equals(currentUser.role());
    }

    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public static boolean isTeamManager() {
        return hasRole("TEAM_MANAGER");
    }

    public static boolean isOrganizer() {
        return hasRole("ORGANIZER");
    }

    /** Only Team Managers and Administrators may create teams (server rule). */
    public static boolean canCreateTeams() {
        return isTeamManager() || isAdmin();
    }

    /** Only Organizers and Administrators may create tournaments (server rule). */
    public static boolean canCreateTournaments() {
        return isOrganizer() || isAdmin();
    }

    /** A tournament can be edited/advanced/deleted by its own organizer or by an Administrator. */
    public static boolean canManageTournament(Long organizerId) {
        return isAdmin() || (isOrganizer() && currentUser.id().equals(organizerId));
    }

    /** A team can be edited/deleted by its own manager or by an Administrator. */
    public static boolean canManageTeam(Long managerId) {
        return isAdmin() || (isTeamManager() && currentUser.id().equals(managerId));
    }
}
