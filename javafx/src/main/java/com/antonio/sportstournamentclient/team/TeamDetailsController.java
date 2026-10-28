package com.antonio.sportstournamentclient.team;

import com.antonio.sportstournamentclient.core.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/** Displays the complete details of a selected team. */
public final class TeamDetailsController {
    @FXML private Label title, teamId, manager, managerId, created, status;

    public void setTeam(long id) {
        var task = BackgroundTask.run(() -> TeamApi.find(id));
        task.setOnSucceeded(e -> {
            var team = task.getValue();
            title.setText(team.teamName());
            teamId.setText(String.valueOf(team.id()));
            manager.setText(team.managerName() == null ? "Unassigned" : team.managerName());
            managerId.setText(team.managerId() == null ? "—" : String.valueOf(team.managerId()));
            created.setText(Formats.dateTime(team.createdAt()));
        });
        task.setOnFailed(e -> Ui.error(status, task.getException(), "Unable to load team details."));
    }
}
