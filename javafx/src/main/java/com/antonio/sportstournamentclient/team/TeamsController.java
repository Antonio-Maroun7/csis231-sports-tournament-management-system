package com.antonio.sportstournamentclient.team;

import com.antonio.sportstournamentclient.core.BackgroundTask;
import com.antonio.sportstournamentclient.core.Formats;
import com.antonio.sportstournamentclient.core.SceneManager;
import com.antonio.sportstournamentclient.core.Ui;
import com.antonio.sportstournamentclient.core.Session;
import com.antonio.sportstournamentclient.team.dto.TeamRequest;
import com.antonio.sportstournamentclient.team.dto.TeamResponse;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/** Full team administration screen. */
public final class TeamsController {
    @FXML private TableView<TeamResponse> teams;
    @FXML private TableColumn<TeamResponse, String> nameColumn, managerColumn, dateColumn, idColumn;
    @FXML private Label status;
    private final javafx.collections.ObservableList<TeamResponse> items = FXCollections.observableArrayList();

    @FXML private void initialize() {
        teams.setItems(items);
        nameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().teamName()));
        managerColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().managerName() == null ? "Unassigned" : c.getValue().managerName()));
        dateColumn.setCellValueFactory(c -> new SimpleStringProperty(Formats.dateTime(c.getValue().createdAt())));
        idColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().id())));
        load();
    }

    @FXML private void load() {
        var task = BackgroundTask.run(TeamApi::all);
        task.setOnSucceeded(e -> { List<TeamResponse> values = task.getValue(); items.setAll(values); status.setText(values.size() + " teams"); });
        task.setOnFailed(e -> Ui.error(status, task.getException(), "Unable to load teams."));
    }

    @FXML private void create() { editDialog(null); }
    @FXML private void edit() { TeamResponse selected = teams.getSelectionModel().getSelectedItem(); if (selected == null) status.setText("Select a team."); else editDialog(selected); }
    @FXML private void open() {
        TeamResponse selected = teams.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Session.setSelectedTeamId(selected.id());
            SceneManager.show("team/team-details-view.fxml");
        }
    }
    @FXML private void delete() {
        TeamResponse selected = teams.getSelectionModel().getSelectedItem();
        if (selected == null) { status.setText("Select a team."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + selected.teamName() + "?", ButtonType.CANCEL, ButtonType.OK);
        confirm.showAndWait().filter(ButtonType.OK::equals).ifPresent(x -> {
            var task = BackgroundTask.run(() -> { TeamApi.delete(selected.id()); return null; });
            task.setOnSucceeded(e -> load()); task.setOnFailed(e -> Ui.error(status, task.getException(), "Unable to delete team."));
        });
    }

    private void editDialog(TeamResponse existing) {
        TextField name = new TextField(existing == null ? "" : existing.teamName());
        TextField manager = new TextField(existing == null || existing.managerId() == null ? "" : String.valueOf(existing.managerId()));
        GridPane form = new GridPane(); form.setHgap(10); form.setVgap(10);
        form.addRow(0, new Label("Team name"), name); form.addRow(1, new Label("Manager ID"), manager);
        Dialog<ButtonType> dialog = new Dialog<>(); dialog.setTitle(existing == null ? "Create team" : "Edit team");
        dialog.getDialogPane().setContent(form); dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.showAndWait().filter(ButtonType.OK::equals).ifPresent(x -> {
            if (name.getText().isBlank()) { status.setText("Team name is required."); return; }
            Long managerId = manager.getText().isBlank() ? null : Long.valueOf(manager.getText());
            var task = BackgroundTask.run(() -> existing == null ? TeamApi.create(new TeamRequest(name.getText(), managerId)) : TeamApi.update(existing.id(), new TeamRequest(name.getText(), managerId)));
            task.setOnSucceeded(e -> load()); task.setOnFailed(e -> Ui.error(status, task.getException(), "Unable to save team."));
        });
    }
}
