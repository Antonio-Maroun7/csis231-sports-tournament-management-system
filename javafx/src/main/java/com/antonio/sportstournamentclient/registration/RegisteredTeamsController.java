package com.antonio.sportstournamentclient.registration;

import com.antonio.sportstournamentclient.core.BackgroundTask;
import com.antonio.sportstournamentclient.core.Formats;
import com.antonio.sportstournamentclient.core.SceneManager;
import com.antonio.sportstournamentclient.core.Session;
import com.antonio.sportstournamentclient.core.Ui;
import com.antonio.sportstournamentclient.registration.dto.RegistrationResponse;
import com.antonio.sportstournamentclient.tournament.TournamentApi;
import com.antonio.sportstournamentclient.tournament.dto.TournamentResponse;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.util.List;

/**
 * "Registered Teams" panel shown inside the Tournament Details View (mockup 09), included with
 * {@code <fx:include source="../registration/registered-teams-panel.fxml"/>}.
 *
 * It lives in the registration feature so the tournament feature never depends on registrations.
 *  - the tournament's Organizer / an Admin approves or rejects PENDING registrations
 *  - a Team Manager opens the Register Team view while the tournament is OPEN
 *  - a team's manager / an Admin withdraws a registration while the tournament is OPEN
 */
public class RegisteredTeamsController {
    private static final String OPEN = "OPEN";

    @FXML private Label summaryLabel;
    @FXML private Button registerButton;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button withdrawButton;
    @FXML private TableView<RegistrationResponse> registrationTable;
    @FXML private TableColumn<RegistrationResponse, String> teamColumn;
    @FXML private TableColumn<RegistrationResponse, String> managerColumn;
    @FXML private TableColumn<RegistrationResponse, String> registeredColumn;
    @FXML private TableColumn<RegistrationResponse, String> statusColumn;
    @FXML private Label panelMessageLabel;

    private TournamentResponse tournament;

    /** What one background call loads: the tournament (status, organizer) and its registrations. */
    private record Data(TournamentResponse tournament, List<RegistrationResponse> registrations) {
    }

    @FXML
    private void initialize() {
        teamColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().teamName()));
        managerColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().managerName()));
        registeredColumn.setCellValueFactory(c -> new SimpleStringProperty(Formats.date(c.getValue().registeredAt())));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status()));
        statusColumn.setCellFactory(column -> new RegistrationStatuses.BadgeCell<>());

        for (Button button : List.of(registerButton, approveButton, rejectButton, withdrawButton)) {
            show(button, false);   // decided once the tournament is loaded
        }
        registrationTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, registration) -> updateRowButtons(registration));

        if (Session.getSelectedTournamentId() != null) {
            loadRegistrations();
        }
    }

    @FXML
    private void loadRegistrations() {
        Long tournamentId = Session.getSelectedTournamentId();
        Ui.info(panelMessageLabel, "Loading registrations...");
        BackgroundTask.run(
                () -> new Data(TournamentApi.getById(tournamentId), RegistrationApi.getByTournament(tournamentId)),
                this::showData,
                error -> Ui.error(panelMessageLabel, error));
    }

    private void showData(Data data) {
        tournament = data.tournament();
        List<RegistrationResponse> registrations = data.registrations();
        registrationTable.getItems().setAll(registrations);

        long approved = registrations.stream().filter(r -> RegistrationStatuses.APPROVED.equals(r.status())).count();
        long pending = registrations.stream().filter(r -> RegistrationStatuses.PENDING.equals(r.status())).count();
        summaryLabel.setText(registrations.size() + " registered  ·  " + approved + " approved  ·  " + pending + " pending");

        boolean open = OPEN.equals(tournament.status());
        boolean organizer = Session.canManageTournament(tournament.organizerId());
        show(approveButton, organizer);
        show(rejectButton, organizer);
        show(registerButton, open && Session.isTeamManager());
        show(withdrawButton, open && (Session.isTeamManager() || Session.isAdmin()));
        updateRowButtons(null);
        panelMessageLabel.setText(open || registrations.isEmpty() ? "" : "Registration is closed for this tournament.");
    }

    /** Approve / Reject only a PENDING row; Withdraw only your own team's row (or any row for an Admin). */
    private void updateRowButtons(RegistrationResponse registration) {
        boolean pending = registration != null && RegistrationStatuses.PENDING.equals(registration.status());
        boolean mine = registration != null
                && (Session.isAdmin() || registration.managerId().equals(Session.getCurrentUser().id()));
        approveButton.setDisable(!pending);
        rejectButton.setDisable(!pending);
        withdrawButton.setDisable(!mine);
    }

    @FXML
    private void handleApprove() {
        decide(true);
    }

    @FXML
    private void handleReject() {
        decide(false);
    }

    private void decide(boolean approve) {
        RegistrationResponse registration = registrationTable.getSelectionModel().getSelectedItem();
        if (registration == null) {
            return;
        }
        BackgroundTask.run(
                () -> approve ? RegistrationApi.approve(registration.id()) : RegistrationApi.reject(registration.id()),
                updated -> {
                    loadRegistrations();
                    Ui.success(panelMessageLabel, updated.teamName() + " " + updated.status().toLowerCase());
                },
                // e.g. 409 "Only a PENDING registration can be approved"
                error -> Ui.error(panelMessageLabel, error));
    }

    @FXML
    private void handleWithdraw() {
        RegistrationResponse registration = registrationTable.getSelectionModel().getSelectedItem();
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
                    loadRegistrations();
                    Ui.success(panelMessageLabel, withdrawn.teamName() + " withdrawn");
                },
                error -> Ui.error(panelMessageLabel, error));
    }

    /** Opens the Tournament Registration View (mockup 10) for this tournament. */
    @FXML
    private void handleRegister() {
        try {
            SceneManager.switchScene("registration/tournament-registration-view.fxml");
        } catch (IOException e) {
            Ui.error(panelMessageLabel, "Could not open registration: " + e.getMessage());
        }
    }

    private static void show(Button button, boolean visible) {
        button.setVisible(visible);
        button.setManaged(visible);
    }
}
