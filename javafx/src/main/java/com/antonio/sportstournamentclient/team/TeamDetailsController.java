package com.antonio.sportstournamentclient.team;

import com.antonio.sportstournamentclient.core.BackgroundTask;
import com.antonio.sportstournamentclient.core.Formats;
import com.antonio.sportstournamentclient.core.SceneManager;
import com.antonio.sportstournamentclient.core.Session;
import com.antonio.sportstournamentclient.core.Ui;
import com.antonio.sportstournamentclient.team.dto.TeamRequest;
import com.antonio.sportstournamentclient.team.dto.TeamResponse;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;

/**
 * Team Details View (mockup 06): information about the team chosen in the Teams View
 * (Session.selectedTeamId), with Edit / Delete for its manager or an Admin.
 * The players section is the roster panel of the player feature (player/roster-panel.fxml).
 */
public class TeamDetailsController {
    @FXML private Label subtitleLabel;
    @FXML private Label teamNameLabel;
    @FXML private Label managerLabel;
    @FXML private Label createdLabel;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Label messageLabel;

    private TeamResponse team;

    @FXML
    private void initialize() {
        setActionsVisible(false);   // shown once we know whether the user may manage this team

        Long teamId = Session.getSelectedTeamId();
        if (teamId == null) {
            Ui.error(messageLabel, "No team selected - go back to Teams and choose one.");
            return;
        }

        Ui.info(messageLabel, "Loading team...");
        BackgroundTask.run(
                () -> TeamApi.getById(teamId),
                this::showTeam,
                error -> Ui.error(messageLabel, error));
    }

    private void showTeam(TeamResponse loaded) {
        team = loaded;
        subtitleLabel.setText(team.teamName());
        teamNameLabel.setText(team.teamName());
        managerLabel.setText(team.managerName());
        createdLabel.setText(Formats.date(team.createdAt()));
        setActionsVisible(Session.canManageTeam(team.managerId()));
        messageLabel.setText("");
    }

    @FXML
    private void handleEdit() {
        if (team == null) {
            return;
        }
        TeamDialog.askTeamName("Edit Team", team.teamName()).ifPresent(name ->
                BackgroundTask.run(
                        () -> TeamApi.update(team.id(), new TeamRequest(name, team.managerId())),
                        updated -> {
                            showTeam(updated);
                            Ui.success(messageLabel, "Team updated");
                        },
                        error -> Ui.error(messageLabel, error)));
    }

    @FXML
    private void handleDelete() {
        if (team == null || !Ui.confirm("Delete Team", "Delete the team '" + team.teamName() + "'?")) {
            return;
        }
        BackgroundTask.run(
                () -> {
                    TeamApi.delete(team.id());
                    return team;
                },
                deleted -> {
                    Session.setSelectedTeamId(null);
                    goBack();
                },
                // e.g. 409 when the team still has players or venues
                error -> Ui.error(messageLabel, error));
    }

    @FXML
    private void handleBack() {
        goBack();
    }

    private void goBack() {
        try {
            SceneManager.switchScene("team/teams-view.fxml");
        } catch (IOException e) {
            Ui.error(messageLabel, "Could not open Teams: " + e.getMessage());
        }
    }

    private void setActionsVisible(boolean visible) {
        editButton.setVisible(visible);
        editButton.setManaged(visible);
        deleteButton.setVisible(visible);
        deleteButton.setManaged(visible);
    }
}
