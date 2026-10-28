package com.antonio.sportstournamentclient.core;

import com.antonio.sportstournamentclient.auth.dto.LoginResponse;
import com.antonio.sportstournamentclient.user.dto.UserResponse;

/** Stores the authenticated user for the current application session. */
public final class Session {
    private static LoginResponse currentUser;
    private static Long selectedTeamId;

    private Session() {
    }

    public static LoginResponse currentUser() {
        return currentUser;
    }

    public static LoginResponse getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(LoginResponse user) {
        currentUser = user;
    }

    public static void setCurrentUser(UserResponse user) {
        currentUser = new LoginResponse(user.id(), user.name(), user.email(), user.role(),
                user.twoFactorEnabled());
    }

    public static boolean isAuthenticated() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return isAuthenticated() && "ADMIN".equals(currentUser.role());
    }

    public static boolean isTeamManager() {
        return isAuthenticated() && "TEAM_MANAGER".equals(currentUser.role());
    }

    public static boolean isOrganizer() {
        return isAuthenticated() && "ORGANIZER".equals(currentUser.role());
    }

    public static boolean canCreateTeams() {
        return isAdmin() || isTeamManager();
    }

    public static boolean canManageTeam(Long managerId) {
        return isAdmin() || (isTeamManager() && currentUser.id().equals(managerId));
    }

    public static Long getSelectedTeamId() {
        return selectedTeamId;
    }

    public static void setSelectedTeamId(Long id) {
        selectedTeamId = id;
    }

    public static void clear() {
        currentUser = null;
        selectedTeamId = null;
    }

    public static void logout() {
        clear();
    }
}
