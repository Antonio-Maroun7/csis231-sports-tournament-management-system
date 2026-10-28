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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

/**
 * Roster panel shown inside the Team Details View (mockup 06), included with
 * {@code <fx:include source="../player/roster-panel.fxml"/>}.
 *
 * It lives in the player feature so the team feature never depends on players
 * (player -> team is allowed, team -> player would create a cycle).
 * It shows the players of Session.selectedTeamId and lets the team's manager / an Admin
 * add, edit and remove them.
 */
public class RosterController {
    @FXML private Label rosterSummaryLabel;
    @FXML private Button addPlayerButton;
    @FXML private Button editPlayerButton;
    @FXML private Button removePlayerButton;
    @FXML private TableView<PlayerResponse> playerTable;
    @FXML private TableColumn<PlayerResponse, Integer> jerseyColumn;
    @FXML private TableColumn<PlayerResponse, String> nameColumn;
    @FXML private TableColumn<PlayerResponse, String> positionColumn;
    @FXML private Label rosterMessageLabel;

    private Long teamId;
    private boolean canManage;

    /** What one background call loads: the team (for permissions) and its players. */
    private record Roster(TeamResponse team, List<PlayerResponse> players) {
    }

    @FXML
    private void initialize() {
        jerseyColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().jerseyNumber()));
        nameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().fullName()));
        positionColumn.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().position() == null ? "-" : c.getValue().position()));

        setManageButtonsVisible(false);   // until we know if the user manages this team
        playerTable.getSelectionModel().selectedItemProperty().addListener((obs, old, player) -> {
            editPlayerButton.setDisable(player == null);
            removePlayerButton.setDisable(player == null);
        });

        teamId = Session.getSelectedTeamId();
        if (teamId != null) {
            loadRoster();
        }
    }

    @FXML
    private void loadRoster() {
        Ui.info(rosterMessageLabel, "Loading players...");
        BackgroundTask.run(
                () -> new Roster(TeamApi.getById(teamId), PlayerApi.getByTeam(teamId)),
                roster -> {
                    canManage = Session.canManageTeam(roster.team().managerId());
                    setManageButtonsVisible(canManage);
                    showPlayers(roster.players());
                },
                error -> Ui.error(rosterMessageLabel, error));
    }

    private void showPlayers(List<PlayerResponse> players) {
        playerTable.getItems().setAll(players);
        int count = players.size();
        rosterSummaryLabel.setText("Roster summary: " + count + (count == 1 ? " player" : " players"));
        rosterMessageLabel.setText("");
        editPlayerButton.setDisable(true);
        removePlayerButton.setDisable(true);
    }

    @FXML
    private void handleAddPlayer() {
        PlayerDialog.askPlayer("Add Player", null).ifPresent(request ->
                BackgroundTask.run(
                        () -> PlayerApi.add(teamId, request),
                        added -> {
                            loadRoster();
                            Ui.success(rosterMessageLabel, added.fullName() + " added");
                        },
                        // e.g. 409 "Jersey number 10 is already taken on Falcons FC"
                        error -> Ui.error(rosterMessageLabel, error)));
    }

    @FXML
    private void handleEditPlayer() {
        PlayerResponse player = playerTable.getSelectionModel().getSelectedItem();
        if (player == null) {
            return;
        }
        PlayerDialog.askPlayer("Edit Player", player).ifPresent(request ->
                BackgroundTask.run(
                        () -> PlayerApi.update(player.id(), request),
                        updated -> {
                            loadRoster();
                            Ui.success(rosterMessageLabel, updated.fullName() + " updated");
                        },
                        error -> Ui.error(rosterMessageLabel, error)));
    }

    @FXML
    private void handleRemovePlayer() {
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
                    loadRoster();
                    Ui.success(rosterMessageLabel, removed.fullName() + " removed");
                },
                error -> Ui.error(rosterMessageLabel, error));
    }

    private void setManageButtonsVisible(boolean visible) {
        for (Button button : List.of(addPlayerButton, editPlayerButton, removePlayerButton)) {
            button.setVisible(visible);
            button.setManaged(visible);
        }
    }
}
