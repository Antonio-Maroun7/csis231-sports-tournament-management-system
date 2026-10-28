package com.antonio.sportstournamentclient.dashboard;

import com.antonio.sportstournamentclient.core.*;
import com.antonio.sportstournamentclient.team.TeamApi;
import com.antonio.sportstournamentclient.user.UserApi;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/** Dashboard cards and navigation controller. */
public final class DashboardController {
    @FXML private Label teamsCount, usersCount, status;
    @FXML private void initialize() {
        var teams = BackgroundTask.run(TeamApi::all);
        teams.setOnSucceeded(e -> teamsCount.setText(String.valueOf(teams.getValue().size())));
        teams.setOnFailed(e -> teamsCount.setText("—"));
        var users = BackgroundTask.run(UserApi::all);
        users.setOnSucceeded(e -> usersCount.setText(String.valueOf(users.getValue().size())));
        users.setOnFailed(e -> usersCount.setText("—"));
    }
    @FXML private void teams() { SceneManager.show("team/teams-view.fxml"); }
    @FXML private void users() { SceneManager.show("user/users-view.fxml"); }
}
