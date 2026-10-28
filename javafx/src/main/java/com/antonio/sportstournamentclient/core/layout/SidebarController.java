package com.antonio.sportstournamentclient.core.layout;

import com.antonio.sportstournamentclient.core.SceneManager;
import com.antonio.sportstournamentclient.core.Session;
import javafx.fxml.FXML;

/** Controller for the shared authenticated navigation sidebar. */
public final class SidebarController {
    @FXML public void dashboard() { SceneManager.show("dashboard/dashboard-view.fxml"); }
    @FXML public void teams() { SceneManager.show("team/teams-view.fxml"); }
    @FXML public void users() { if (Session.isAdmin()) SceneManager.show("user/users-view.fxml"); }
    @FXML public void logout() { Session.logout(); SceneManager.show("auth/login-view.fxml"); }
}
