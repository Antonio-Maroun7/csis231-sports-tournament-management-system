package com.antonio.sportstournamentclient.core;

import com.antonio.sportstournamentclient.auth.dto.LoginResponse;

/**
 * Who is logged in, plus small values passed between screens (same role as the instructor's Session).
 */
public class Session {
    private static LoginResponse currentUser;

    // team chosen in the Teams view, shown by the Team Details view
    private static Long selectedTeamId;

    public static void setCurrentUser(LoginResponse user) {
        currentUser = user;
    }

    public static LoginResponse getCurrentUser() {
        return currentUser;
    }

    public static Long getSelectedTeamId() {
        return selectedTeamId;
    }

    public static void setSelectedTeamId(Long teamId) {
        selectedTeamId = teamId;
    }

    public static void clear() {
        currentUser = null;
        selectedTeamId = null;
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

    /** A team can be edited/deleted by its own manager or by an Administrator. */
    public static boolean canManageTeam(Long managerId) {
        return isAdmin() || (isTeamManager() && currentUser.id().equals(managerId));
    }
}
