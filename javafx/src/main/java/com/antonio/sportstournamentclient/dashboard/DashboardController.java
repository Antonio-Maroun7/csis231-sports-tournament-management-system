package com.antonio.sportstournamentclient.dashboard;

import com.antonio.sportstournamentclient.auth.dto.LoginResponse;
import com.antonio.sportstournamentclient.core.BackgroundTask;
import com.antonio.sportstournamentclient.core.Formats;
import com.antonio.sportstournamentclient.core.SceneManager;
import com.antonio.sportstournamentclient.core.Session;
import com.antonio.sportstournamentclient.core.Ui;
import com.antonio.sportstournamentclient.team.TeamApi;
import com.antonio.sportstournamentclient.team.TeamDialog;
import com.antonio.sportstournamentclient.team.dto.TeamRequest;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;

/**
 * Dashboard View (mockup 04): landing page after login, with role-based cards and quick actions.
 */
public class DashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Label teamsTitleLabel;
    @FXML private Label teamsCountLabel;
    @FXML private Label roleLabel;
    @FXML private Label emailLabel;
    @FXML private Button createTeamButton;
    @FXML private Button usersButton;
    @FXML private Label messageLabel;

    @FXML
    private void initialize() {
        LoginResponse user = Session.getCurrentUser();
        if (user == null) {
            return;
        }
        String firstName = user.name().split("\\s+")[0];
        welcomeLabel.setText("Welcome back, " + firstName);
        roleLabel.setText(Formats.role(user.role()));
        emailLabel.setText(user.email());

        // quick actions depend on the role
        boolean canCreate = Session.canCreateTeams();
        createTeamButton.setVisible(canCreate);
        createTeamButton.setManaged(canCreate);
        usersButton.setVisible(Session.isAdmin());
        usersButton.setManaged(Session.isAdmin());

        teamsTitleLabel.setText(Session.isTeamManager() ? "My Teams" : "Teams");
        loadTeamCount();
    }

    private void loadTeamCount() {
        Long me = Session.getCurrentUser().id();
        boolean onlyMine = Session.isTeamManager();
        teamsCountLabel.setText("...");
        BackgroundTask.run(
                () -> onlyMine ? TeamApi.getByManager(me) : TeamApi.getAll(),
                teams -> teamsCountLabel.setText(String.valueOf(teams.size())),
                error -> {
                    teamsCountLabel.setText("-");
                    Ui.error(messageLabel, error);
                });
    }

    @FXML
    private void handleCreateTeam() {
        TeamDialog.askTeamName("Create Team", "").ifPresent(name -> {
            Long me = Session.getCurrentUser().id();
            BackgroundTask.run(
                    () -> TeamApi.create(new TeamRequest(name, me)),
                    created -> {
                        Ui.success(messageLabel, "Team '" + created.teamName() + "' created");
                        loadTeamCount();
                    },
                    error -> Ui.error(messageLabel, error));
        });
    }

    @FXML
    private void handleViewTeams() throws IOException {
        SceneManager.switchScene("team/teams-view.fxml");
    }

    @FXML
    private void handleUsers() throws IOException {
        SceneManager.switchScene("user/users-view.fxml");
    }
}
