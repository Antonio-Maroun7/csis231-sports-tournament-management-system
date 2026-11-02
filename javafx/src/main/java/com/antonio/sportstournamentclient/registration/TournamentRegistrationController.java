package com.antonio.sportstournamentclient.registration;

import com.antonio.sportstournamentclient.core.BackgroundTask;
import com.antonio.sportstournamentclient.core.Formats;
import com.antonio.sportstournamentclient.core.SceneManager;
import com.antonio.sportstournamentclient.core.Session;
import com.antonio.sportstournamentclient.core.Ui;
import com.antonio.sportstournamentclient.registration.dto.RegistrationResponse;
import com.antonio.sportstournamentclient.team.TeamApi;
import com.antonio.sportstournamentclient.team.dto.TeamResponse;
import com.antonio.sportstournamentclient.tournament.TournamentApi;
import com.antonio.sportstournamentclient.tournament.dto.TournamentResponse;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Tournament Registration View (mockup 10): a Team Manager picks one of their teams and an OPEN
 * tournament and registers it (the registration starts PENDING until the Organizer approves it).
 * Below the form, "My Registrations" lists every registration of the manager's teams, with Withdraw.
 */
public class TournamentRegistrationController {
    private static final String OPEN = "OPEN";

    @FXML private Label subtitleLabel;
    @FXML private ComboBox<TournamentResponse> tournamentCombo;
    @FXML private ComboBox<TeamResponse> teamCombo;
    @FXML private Button registerButton;
    @FXML private Label formMessageLabel;
    @FXML private TableView<RegistrationResponse> myRegistrationsTable;
    @FXML private TableColumn<RegistrationResponse, String> tournamentColumn;
    @FXML private TableColumn<RegistrationResponse, String> teamColumn;
    @FXML private TableColumn<RegistrationResponse, String> registeredColumn;
    @FXML private TableColumn<RegistrationResponse, String> statusColumn;
    @FXML private Button withdrawButton;
    @FXML private Label messageLabel;

    /** Everything the view needs, loaded in one background call. */
    private record Data(List<TournamentResponse> openTournaments,
                        List<TeamResponse> myTeams,
                        List<RegistrationResponse> myRegistrations) {
    }

    /** Fills the two combo boxes (open tournaments, the manager's teams), binds "My Registrations" and loads the data; non-managers get a message instead. */
    @FXML
    private void initialize() {
        Ui.displayAs(tournamentCombo, TournamentResponse::name);
        Ui.displayAs(teamCombo, TeamResponse::teamName);
        tournamentCombo.valueProperty().addListener((obs, old, t) -> onFormChanged());
        teamCombo.valueProperty().addListener((obs, old, t) -> onFormChanged());
        registerButton.setDisable(true);

        tournamentColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().tournamentName()));
        teamColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().teamName()));
        registeredColumn.setCellValueFactory(c -> new SimpleStringProperty(Formats.date(c.getValue().registeredAt())));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status()));
        statusColumn.setCellFactory(column -> new RegistrationStatuses.BadgeCell<>());

        // a team can only withdraw while the tournament is still OPEN
        withdrawButton.setDisable(true);
        myRegistrationsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, r) ->
                withdrawButton.setDisable(r == null || !OPEN.equals(r.tournamentStatus())));

        if (!Session.isTeamManager() && !Session.isAdmin()) {
            Ui.error(formMessageLabel, "Only Team Managers can register teams.");
            tournamentCombo.setDisable(true);
            teamCombo.setDisable(true);
            return;
        }
        loadData();
    }

    private void loadData() {
        Long me = Session.getCurrentUser().id();
        boolean admin = Session.isAdmin();
        Ui.info(messageLabel, "Loading...");
        BackgroundTask.run(
                () -> {
                    List<TeamResponse> teams = admin ? TeamApi.getAll() : TeamApi.getByManager(me);
                    List<RegistrationResponse> registrations = new ArrayList<>();
                    for (TeamResponse team : teams) {
                        registrations.addAll(RegistrationApi.getByTeam(team.id()));
                    }
                    registrations.sort(Comparator.comparing(RegistrationResponse::registeredAt).reversed());
                    return new Data(TournamentApi.getByStatus(OPEN), teams, registrations);
                },
                this::showData,
                error -> Ui.error(messageLabel, error));
    }

    private void showData(Data data) {
        TournamentResponse previousTournament = tournamentCombo.getValue();
        TeamResponse previousTeam = teamCombo.getValue();
        tournamentCombo.getItems().setAll(data.openTournaments());
        teamCombo.getItems().setAll(data.myTeams());
        myRegistrationsTable.getItems().setAll(data.myRegistrations());

        // preselect the tournament we came from (Tournament Details -> Register Team)
        Long wanted = previousTournament != null ? previousTournament.id() : Session.getSelectedTournamentId();
        for (TournamentResponse tournament : data.openTournaments()) {
            if (tournament.id().equals(wanted)) {
                tournamentCombo.setValue(tournament);
            }
        }
        if (previousTeam != null) {
            for (TeamResponse team : data.myTeams()) {
                if (team.id().equals(previousTeam.id())) {
                    teamCombo.setValue(team);
                }
            }
        } else if (data.myTeams().size() == 1) {
            teamCombo.setValue(data.myTeams().get(0));
        }

        if (data.openTournaments().isEmpty()) {
            Ui.info(formMessageLabel, "No tournament is open for registration right now.");
        } else if (data.myTeams().isEmpty()) {
            Ui.info(formMessageLabel, "You have no team yet - create a team first.");
        }
        int count = data.myRegistrations().size();
        Ui.info(messageLabel, count + (count == 1 ? " registration" : " registrations"));
    }

    private void onFormChanged() {
        TournamentResponse tournament = tournamentCombo.getValue();
        registerButton.setDisable(tournament == null || teamCombo.getValue() == null);
        subtitleLabel.setText(tournament == null ? "Register a team in an open tournament"
                : "Register a team in " + tournament.name());
    }

    /** "Register Team" button: registers the chosen team in the chosen tournament with POST /api/tournaments/{id}/registrations in the background (it starts PENDING). */
    @FXML
    private void handleRegister() {
        TournamentResponse tournament = tournamentCombo.getValue();
        TeamResponse team = teamCombo.getValue();
        if (tournament == null || team == null) {
            return;
        }
        registerButton.setDisable(true);
        BackgroundTask.run(
                () -> RegistrationApi.register(tournament.id(), team.id()),
                registration -> {
                    registerButton.setDisable(false);
                    Ui.success(formMessageLabel, registration.teamName() + " registered in "
                            + registration.tournamentName() + " - waiting for the organizer's approval.");
                    loadData();
                },
                error -> {
                    registerButton.setDisable(false);
                    // e.g. 409 "'Falcons FC' is already registered in 'Autumn Cup 2026'"
                    Ui.error(formMessageLabel, error);
                });
    }

    /** "Withdraw" button: after a confirmation, calls DELETE /api/registrations/{id} for the selected row of "My Registrations". */
    @FXML
    private void handleWithdraw() {
        RegistrationResponse registration = myRegistrationsTable.getSelectionModel().getSelectedItem();
        if (registration == null || !Ui.confirm("Withdraw Team",
                "Withdraw " + registration.teamName() + " from " + registration.tournamentName() + "?")) {
            return;
        }
        BackgroundTask.run(
                () -> {
                    RegistrationApi.withdraw(registration.id());
                    return registration;
                },
                withdrawn -> {
                    Ui.success(messageLabel, withdrawn.teamName() + " withdrawn from " + withdrawn.tournamentName());
                    loadData();
                },
                error -> Ui.error(messageLabel, error));
    }

    /** "Cancel" button: goes back to the tournament we came from, otherwise to the Tournaments list. */
    @FXML
    private void handleCancel() {
        try {
            if (Session.getSelectedTournamentId() != null) {
                SceneManager.switchScene("tournament/tournament-details-view.fxml");
            } else {
                SceneManager.switchScene("tournament/tournaments-view.fxml");
            }
        } catch (IOException e) {
            Ui.error(formMessageLabel, "Could not go back: " + e.getMessage());
        }
    }
}
