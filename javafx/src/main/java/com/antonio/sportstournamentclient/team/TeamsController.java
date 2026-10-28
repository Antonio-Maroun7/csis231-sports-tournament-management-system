package com.antonio.sportstournamentclient.team;

import com.antonio.sportstournamentclient.core.BackgroundTask;
import com.antonio.sportstournamentclient.core.Formats;
import com.antonio.sportstournamentclient.core.SceneManager;
import com.antonio.sportstournamentclient.core.Session;
import com.antonio.sportstournamentclient.core.Ui;
import com.antonio.sportstournamentclient.team.dto.TeamRequest;
import com.antonio.sportstournamentclient.team.dto.TeamResponse;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.util.List;

/**
 * Teams View (mockup 05): lists teams and lets Team Managers / Admins create, edit and delete them.
 * A Team Manager sees only his own teams; Admins and Organizers see all teams.
 * Double-click a row to open the Team Details View.
 */
public class TeamsController {
    @FXML private Label subtitleLabel;
    @FXML private TableView<TeamResponse> teamTable;
    @FXML private TableColumn<TeamResponse, String> nameColumn;
    @FXML private TableColumn<TeamResponse, String> managerColumn;
    @FXML private TableColumn<TeamResponse, String> createdColumn;
    @FXML private Button createButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Label messageLabel;

    @FXML
    private void initialize() {
        // data binding: one column = one field of the TeamResponse record
        nameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().teamName()));
        managerColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().managerName()));
        createdColumn.setCellValueFactory(c -> new SimpleStringProperty(Formats.date(c.getValue().createdAt())));

        // only Team Managers and Admins can change teams; Organizers only read
        boolean canWrite = Session.canCreateTeams();
        for (Button button : List.of(createButton, editButton, deleteButton)) {
            button.setVisible(canWrite);
            button.setManaged(canWrite);
        }

        // Edit / Delete are enabled only for a selected team the user may manage
        teamTable.getSelectionModel().selectedItemProperty().addListener((obs, old, team) -> updateRowButtons(team));
        updateRowButtons(null);

        // double-click -> Team Details
        teamTable.setRowFactory(table -> {
            TableRow<TeamResponse> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openDetails(row.getItem());
                }
            });
            return row;
        });

        subtitleLabel.setText(Session.isTeamManager() ? "Teams you manage" : "All teams in the system");
        loadTeams();
    }

    @FXML
    private void loadTeams() {
        Ui.info(messageLabel, "Loading teams...");
        Long me = Session.getCurrentUser().id();
        boolean onlyMine = Session.isTeamManager();

        BackgroundTask.run(
                () -> onlyMine ? TeamApi.getByManager(me) : TeamApi.getAll(),
                teams -> {
                    teamTable.getItems().setAll(teams);
                    Ui.info(messageLabel, teams.size() + (teams.size() == 1 ? " team loaded" : " teams loaded"));;
                },
                error -> Ui.error(messageLabel, error));
    }

    @FXML
    private void handleCreate() {
        TeamDialog.askTeamName("Create Team", "").ifPresent(name -> {
            Long me = Session.getCurrentUser().id();
            createButton.setDisable(true);
            BackgroundTask.run(
                    () -> TeamApi.create(new TeamRequest(name, me)),
                    created -> {
                        createButton.setDisable(false);
                        Ui.success(messageLabel, "Team '" + created.teamName() + "' created");
                        loadTeams();
                    },
                    error -> {
                        createButton.setDisable(false);
                        Ui.error(messageLabel, error);
                    });
        });
    }

    @FXML
    private void handleEdit() {
        TeamResponse team = teamTable.getSelectionModel().getSelectedItem();
        if (team == null) {
            return;
        }
        TeamDialog.askTeamName("Edit Team", team.teamName()).ifPresent(name ->
                BackgroundTask.run(
                        // keep the same manager, only the name changes
                        () -> TeamApi.update(team.id(), new TeamRequest(name, team.managerId())),
                        updated -> {
                            Ui.success(messageLabel, "Team renamed to '" + updated.teamName() + "'");
                            loadTeams();
                        },
                        error -> Ui.error(messageLabel, error)));
    }

    @FXML
    private void handleDelete() {
        TeamResponse team = teamTable.getSelectionModel().getSelectedItem();
        if (team == null || !Ui.confirm("Delete Team", "Delete the team '" + team.teamName() + "'?")) {
            return;
        }
        BackgroundTask.run(
                () -> {
                    TeamApi.delete(team.id());
                    return team;
                },
                deleted -> {
                    Ui.success(messageLabel, "Team '" + deleted.teamName() + "' deleted");
                    loadTeams();
                },
                // e.g. 409 when the team still has players or venues
                error -> Ui.error(messageLabel, error));
    }

    @FXML
    private void handleOpenDetails() {
        TeamResponse team = teamTable.getSelectionModel().getSelectedItem();
        if (team == null) {
            Ui.error(messageLabel, "Select a team first");
            return;
        }
        openDetails(team);
    }

    private void openDetails(TeamResponse team) {
        Session.setSelectedTeamId(team.id());
        try {
            SceneManager.switchScene("team/team-details-view.fxml");
        } catch (IOException e) {
            Ui.error(messageLabel, "Could not open team details: " + e.getMessage());
        }
    }

    private void updateRowButtons(TeamResponse team) {
        boolean manageable = team != null && Session.canManageTeam(team.managerId());
        editButton.setDisable(!manageable);
        deleteButton.setDisable(!manageable);
    }
}
