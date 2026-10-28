package com.antonio.sportstournamentclient.core.layout;

import com.antonio.sportstournamentclient.auth.dto.LoginResponse;
import com.antonio.sportstournamentclient.core.Formats;
import com.antonio.sportstournamentclient.core.SceneManager;
import com.antonio.sportstournamentclient.core.Session;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;

/**
 * Left navigation bar shared by every screen after login (included with fx:include).
 * Highlights the current page, hides "Users" for non-admins, shows the logged-in user.
 */
public class SidebarController {
    @FXML private Button dashboardButton;
    @FXML private Button teamsButton;
    @FXML private Button playersButton;
    @FXML private Button usersButton;
    @FXML private Label avatarLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    @FXML
    private void initialize() {
        LoginResponse user = Session.getCurrentUser();
        if (user != null) {
            avatarLabel.setText(Formats.initials(user.name()));
            userNameLabel.setText(user.name());
            userRoleLabel.setText(Formats.role(user.role()));
        }

        // "Users" is an administrator screen
        boolean admin = Session.isAdmin();
        usersButton.setVisible(admin);
        usersButton.setManaged(admin);

        highlightCurrentPage();
    }

    private void highlightCurrentPage() {
        String view = SceneManager.getCurrentView();
        if (view == null) {
            return;
        }
        if (view.startsWith("dashboard/")) {
            dashboardButton.getStyleClass().add("active");
        } else if (view.startsWith("team/")) {
            teamsButton.getStyleClass().add("active");
        } else if (view.startsWith("player/")) {
            playersButton.getStyleClass().add("active");
        } else if (view.startsWith("user/")) {
            usersButton.getStyleClass().add("active");
        }
    }

    @FXML
    private void handleDashboard() throws IOException {
        SceneManager.switchScene("dashboard/dashboard-view.fxml");
    }

    @FXML
    private void handleTeams() throws IOException {
        SceneManager.switchScene("team/teams-view.fxml");
    }

    @FXML
    private void handlePlayers() throws IOException {
        SceneManager.switchScene("player/players-view.fxml");
    }

    @FXML
    private void handleUsers() throws IOException {
        SceneManager.switchScene("user/users-view.fxml");
    }

    @FXML
    private void handleLogout() throws IOException {
        Session.clear();
        SceneManager.switchScene("auth/login-view.fxml");
    }
}
