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
import com.antonio.sportstournamentclient.tournament.TournamentApi;
import com.antonio.sportstournamentclient.tournament.TournamentStatuses;
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
    @FXML private Label tournamentsCountLabel;
    @FXML private Label roleLabel;
    @FXML private Label emailLabel;
    @FXML private Button createTeamButton;
    @FXML private Button usersButton;
    @FXML private Label messageLabel;

    /** Loads the team and tournament counters for the logged-in user and shows the role-based quick actions. */
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
        loadActiveTournamentCount();
    }

    /** "Active" = OPEN for registration or IN_PROGRESS. */
    private void loadActiveTournamentCount() {
        tournamentsCountLabel.setText("...");
        BackgroundTask.run(
                () -> TournamentApi.getAll().stream()
                        .filter(t -> TournamentStatuses.OPEN.equals(t.status())
                                || TournamentStatuses.IN_PROGRESS.equals(t.status()))
                        .count(),
                count -> tournamentsCountLabel.setText(String.valueOf(count)),
                error -> {
                    tournamentsCountLabel.setText("-");
                    Ui.error(messageLabel, error);
                });
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

    /** "+ Create Team" quick action (Team Managers and Admins): asks for a name, then calls POST /api/teams in the background and refreshes the team counter. */
    @FXML
    private void handleCreateTeam() {
        TeamDialog.askTeamName("Create Team", "").ifPresent(name -> {
            BackgroundTask.run(
                    () -> TeamApi.create(new TeamRequest(name)),
                    created -> {
                        Ui.success(messageLabel, "Team '" + created.teamName() + "' created");
                        loadTeamCount();
                    },
                    error -> Ui.error(messageLabel, error));
        });
    }

    /** "View Teams" button: opens the teams view. */
    @FXML
    private void handleViewTeams() throws IOException {
        SceneManager.switchScene("team/teams-view.fxml");
    }

    /** "Browse Tournaments" button: opens the tournaments view. */
    @FXML
    private void handleTournaments() throws IOException {
        SceneManager.switchScene("tournament/tournaments-view.fxml");
    }

    /** "Manage Users" button (administrators only): opens the users view. */
    @FXML
    private void handleUsers() throws IOException {
        SceneManager.switchScene("user/users-view.fxml");
    }
}
