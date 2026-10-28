package com.antonio.sportstournamentclient.player;

import com.antonio.sportstournamentclient.core.BackgroundTask;
import com.antonio.sportstournamentclient.core.Session;
import com.antonio.sportstournamentclient.core.Ui;
import com.antonio.sportstournamentclient.player.dto.PlayerResponse;
import com.antonio.sportstournamentclient.team.TeamApi;
import com.antonio.sportstournamentclient.team.dto.TeamResponse;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

/**
 * Players View (mockup 07): choose a team, see its roster, and (for the team's manager / an Admin)
 * add, edit or remove players. Team Managers can choose among their own teams; others see all teams.
 */
public class PlayersController {
    @FXML private Label subtitleLabel;
    @FXML private ComboBox<TeamResponse> teamCombo;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button removeButton;
    @FXML private TableView<PlayerResponse> playerTable;
    @FXML private TableColumn<PlayerResponse, Integer> jerseyColumn;
    @FXML private TableColumn<PlayerResponse, String> nameColumn;
    @FXML private TableColumn<PlayerResponse, String> teamColumn;
    @FXML private TableColumn<PlayerResponse, String> positionColumn;
    @FXML private Label messageLabel;

    @FXML
    private void initialize() {
        jerseyColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().jerseyNumber()));
        nameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().fullName()));
        teamColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().teamName()));
        positionColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().position() == null ? "-" : c.getValue().position()));

        // show the team NAME in the combo box (list and selected value)
        teamCombo.setCellFactory(list -> new TeamCell());
        teamCombo.setButtonCell(new TeamCell());
        teamCombo.valueProperty().addListener((obs, old, team) -> onTeamSelected(team));

        playerTable.getSelectionModel().selectedItemProperty().addListener((obs, old, player) -> updateRowButtons());
        setManageButtonsVisible(false);
        loadTeams();
    }

    private void loadTeams() {
        Ui.info(messageLabel, "Loading teams...");
        Long me = Session.getCurrentUser().id();
        boolean onlyMine = Session.isTeamManager();
        BackgroundTask.run(
                () -> onlyMine ? TeamApi.getByManager(me) : TeamApi.getAll(),
                teams -> {
                    teamCombo.getItems().setAll(teams);
                    if (teams.isEmpty()) {
                        Ui.info(messageLabel, "No teams yet - create a team first.");
                        return;
                    }
                    teamCombo.setValue(findTeam(teams, Session.getSelectedTeamId()));
                },
                error -> Ui.error(messageLabel, error));
    }

    /** The team chosen earlier (e.g. on Team Details), otherwise the first one. */
    private TeamResponse findTeam(List<TeamResponse> teams, Long teamId) {
        for (TeamResponse team : teams) {
            if (team.id().equals(teamId)) {
                return team;
            }
        }
        return teams.get(0);
    }

    private void onTeamSelected(TeamResponse team) {
        playerTable.getItems().clear();
        if (team == null) {
            setManageButtonsVisible(false);
            return;
        }
        Session.setSelectedTeamId(team.id());
        subtitleLabel.setText(team.teamName() + " roster");
        setManageButtonsVisible(Session.canManageTeam(team.managerId()));
        loadPlayers();
    }

    @FXML
    private void loadPlayers() {
        TeamResponse team = teamCombo.getValue();
        if (team == null) {
            return;
        }
        Ui.info(messageLabel, "Loading players...");
        BackgroundTask.run(
                () -> PlayerApi.getByTeam(team.id()),
                players -> {
                    playerTable.getItems().setAll(players);
                    int count = players.size();
                    Ui.info(messageLabel, count + (count == 1 ? " player" : " players") + " in " + team.teamName());
                    updateRowButtons();
                },
                error -> Ui.error(messageLabel, error));
    }

    @FXML
    private void handleAdd() {
        TeamResponse team = teamCombo.getValue();
        if (team == null) {
            return;
        }
        PlayerDialog.askPlayer("Add Player to " + team.teamName(), null).ifPresent(request ->
                BackgroundTask.run(
                        () -> PlayerApi.add(team.id(), request),
                        added -> {
                            loadPlayers();
                            Ui.success(messageLabel, added.fullName() + " added");
                        },
                        // e.g. 409 "Jersey number 10 is already taken on Falcons FC"
                        error -> Ui.error(messageLabel, error)));
    }

    @FXML
    private void handleEdit() {
        PlayerResponse player = playerTable.getSelectionModel().getSelectedItem();
        if (player == null) {
            return;
        }
        PlayerDialog.askPlayer("Edit Player", player).ifPresent(request ->
                BackgroundTask.run(
                        () -> PlayerApi.update(player.id(), request),
                        updated -> {
                            loadPlayers();
                            Ui.success(messageLabel, updated.fullName() + " updated");
                        },
                        error -> Ui.error(messageLabel, error)));
    }

    @FXML
    private void handleRemove() {
        PlayerResponse player = playerTable.getSelectionModel().getSelectedItem();
        if (player == null || !Ui.confirm("Remove Player", "Remove " + player.fullName() + " from the roster?")) {
            return;
        }
        BackgroundTask.run(
                () -> {
                    PlayerApi.remove(player.id());
                    return player;
                },
                removed -> {
                    loadPlayers();
                    Ui.success(messageLabel, removed.fullName() + " removed");
                },
                error -> Ui.error(messageLabel, error));
    }

    private void updateRowButtons() {
        boolean selected = playerTable.getSelectionModel().getSelectedItem() != null;
        editButton.setDisable(!selected);
        removeButton.setDisable(!selected);
    }

    private void setManageButtonsVisible(boolean visible) {
        for (Button button : List.of(addButton, editButton, removeButton)) {
            button.setVisible(visible);
            button.setManaged(visible);
        }
    }

    /** Combo box cell that displays a team by its name. */
    private static class TeamCell extends ListCell<TeamResponse> {
        @Override
        protected void updateItem(TeamResponse team, boolean empty) {
            super.updateItem(team, empty);
            setText(empty || team == null ? null : team.teamName());
        }
    }
}
