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

    private static String flashMessage;

    /**
     * Stores the user returned by a successful login (including the JWT).
     *
     * @param user the logged-in user, or {@code null} to log out
     */
    public static void setCurrentUser(LoginResponse user) {
        currentUser = user;
    }

    /**
     * Returns the logged-in user.
     *
     * @return the current user, or {@code null} when nobody is logged in
     */
    public static LoginResponse getCurrentUser() {
        return currentUser;
    }

    /** The JWT of the logged-in user (kept in memory only, never saved to disk), or null. */
    public static String getToken() {
        return currentUser == null ? null : currentUser.token();
    }

    /** One-shot message shown once on the Login view (e.g. "session expired"), then forgotten. */
    public static void setFlashMessage(String message) {
        flashMessage = message;
    }

    /**
     * Reads and erases the one-shot message, so it is shown only once.
     *
     * @return the pending message, or {@code null} if there is none
     */
    public static String consumeFlashMessage() {
        String message = flashMessage;
        flashMessage = null;
        return message;
    }

    /**
     * Returns the login that is waiting for its emailed 2FA code.
     *
     * @return the pending challenge, or {@code null} if no 2FA login is in progress
     */
    public static AuthResult getPendingChallenge() {
        return pendingChallenge;
    }

    /**
     * Remembers a login that needs the emailed 2FA code (set by the login view, read by the 2FA view).
     *
     * @param challenge the server's 2FA challenge answer, or {@code null} to forget it
     */
    public static void setPendingChallenge(AuthResult challenge) {
        pendingChallenge = challenge;
    }

    /**
     * Returns the team chosen in the Teams view (shown by the Team Details view).
     *
     * @return the team id, or {@code null} if none was chosen
     */
    public static Long getSelectedTeamId() {
        return selectedTeamId;
    }

    /**
     * Remembers the team chosen in the Teams view.
     *
     * @param teamId id of the chosen team
     */
    public static void setSelectedTeamId(Long teamId) {
        selectedTeamId = teamId;
    }

    /**
     * Returns the tournament chosen in the Tournaments view (shown by the Tournament Details view).
     *
     * @return the tournament id, or {@code null} if none was chosen
     */
    public static Long getSelectedTournamentId() {
        return selectedTournamentId;
    }

    /**
     * Remembers the tournament chosen in the Tournaments view.
     *
     * @param tournamentId id of the chosen tournament
     */
    public static void setSelectedTournamentId(Long tournamentId) {
        selectedTournamentId = tournamentId;
    }

    /** Logs out: forgets the user (and its JWT), the pending 2FA login and the selected team and tournament. */
    public static void clear() {
        currentUser = null;
        pendingChallenge = null;
        selectedTeamId = null;
        selectedTournamentId = null;
    }

    // -------- role helpers (the server still enforces its own rules) --------

    /**
     * Checks the role of the logged-in user.
     *
     * @param role {@code ADMIN}, {@code ORGANIZER} or {@code TEAM_MANAGER}
     * @return {@code true} if a user is logged in and has exactly this role
     */
    public static boolean hasRole(String role) {
        return currentUser != null && role.equals(currentUser.role());
    }

    /**
     * Checks for the Administrator role.
     *
     * @return {@code true} if the logged-in user is an ADMIN
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Checks for the Team Manager role.
     *
     * @return {@code true} if the logged-in user is a TEAM_MANAGER
     */
    public static boolean isTeamManager() {
        return hasRole("TEAM_MANAGER");
    }

    /**
     * Checks for the Organizer role.
     *
     * @return {@code true} if the logged-in user is an ORGANIZER
     */
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
