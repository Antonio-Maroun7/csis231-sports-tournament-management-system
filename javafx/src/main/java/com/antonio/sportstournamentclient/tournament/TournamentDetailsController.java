package com.antonio.sportstournamentclient.tournament;

import com.antonio.sportstournamentclient.core.BackgroundTask;
import com.antonio.sportstournamentclient.core.Formats;
import com.antonio.sportstournamentclient.core.SceneManager;
import com.antonio.sportstournamentclient.core.Session;
import com.antonio.sportstournamentclient.core.Ui;
import com.antonio.sportstournamentclient.tournament.dto.TournamentRequest;
import com.antonio.sportstournamentclient.tournament.dto.TournamentResponse;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.util.List;

/**
 * Tournament Details View (mockup 09): information about the tournament chosen in the
 * Tournaments View (Session.selectedTournamentId). Its organizer / an Admin can edit it,
 * move it to the next status, or delete it. The "Registered Teams" section is the
 * registration feature's panel (registration/registered-teams-panel.fxml).
 */
public class TournamentDetailsController {
    @FXML private Label subtitleLabel;
    @FXML private Label organizerLabel;
    @FXML private Label startLabel;
    @FXML private Label endLabel;
    @FXML private Label statusBadge;
    @FXML private Button editButton;
    @FXML private Button advanceButton;
    @FXML private Button deleteButton;
    @FXML private Label messageLabel;

    private TournamentResponse tournament;

    @FXML
    private void initialize() {
        setActionsVisible(false);   // shown once we know whether the user may manage it

        Long tournamentId = Session.getSelectedTournamentId();
        if (tournamentId == null) {
            Ui.error(messageLabel, "No tournament selected - go back to Tournaments and choose one.");
            return;
        }
        Ui.info(messageLabel, "Loading tournament...");
        BackgroundTask.run(
                () -> TournamentApi.getById(tournamentId),
                this::showTournament,
                error -> Ui.error(messageLabel, error));
    }

    private void showTournament(TournamentResponse loaded) {
        tournament = loaded;
        subtitleLabel.setText(tournament.name());
        organizerLabel.setText(tournament.organizerName());
        startLabel.setText(Formats.date(tournament.startDate()));
        endLabel.setText(Formats.date(tournament.endDate()));
        TournamentStatuses.styleBadge(statusBadge, tournament.status());

        boolean manageable = Session.canManageTournament(tournament.organizerId());
        boolean completed = TournamentStatuses.COMPLETED.equals(tournament.status());
        setActionsVisible(manageable);
        editButton.setDisable(completed);
        advanceButton.setText(TournamentStatuses.actionText(tournament.status()));
        advanceButton.setDisable(completed);
        messageLabel.setText("");
    }

    @FXML
    private void handleEdit() {
        if (tournament == null) {
            return;
        }
        TournamentDialog.askTournament("Edit Tournament", tournament).ifPresent(form -> {
            TournamentRequest request = new TournamentRequest(form.name(), form.startDate(), form.endDate(),
                    tournament.organizerId());
            BackgroundTask.run(
                    () -> TournamentApi.update(tournament.id(), request),
                    updated -> {
                        showTournament(updated);
                        Ui.success(messageLabel, "Tournament updated");
                    },
                    error -> Ui.error(messageLabel, error));
        });
    }

    @FXML
    private void handleAdvance() {
        if (tournament == null) {
            return;
        }
        String next = TournamentStatuses.next(tournament.status());
        if (next == null || !Ui.confirm(TournamentStatuses.actionText(tournament.status()),
                "Move '" + tournament.name() + "' to " + TournamentStatuses.label(next) + "?")) {
            return;
        }
        BackgroundTask.run(
                () -> TournamentApi.changeStatus(tournament.id(), next),
                updated -> {
                    showTournament(updated);
                    Ui.success(messageLabel, "Status changed to " + TournamentStatuses.label(updated.status()));
                },
                error -> Ui.error(messageLabel, error));
    }

    @FXML
    private void handleDelete() {
        if (tournament == null || !Ui.confirm("Delete Tournament", "Delete '" + tournament.name() + "'?")) {
            return;
        }
        BackgroundTask.run(
                () -> {
                    TournamentApi.delete(tournament.id());
                    return tournament;
                },
                deleted -> {
                    Session.setSelectedTournamentId(null);
                    goBack();
                },
                // e.g. 409 when teams are already registered
                error -> Ui.error(messageLabel, error));
    }

    @FXML
    private void handleBack() {
        goBack();
    }

    private void goBack() {
        try {
            SceneManager.switchScene("tournament/tournaments-view.fxml");
        } catch (IOException e) {
            Ui.error(messageLabel, "Could not open Tournaments: " + e.getMessage());
        }
    }

    private void setActionsVisible(boolean visible) {
        for (Button button : List.of(editButton, advanceButton, deleteButton)) {
            button.setVisible(visible);
            button.setManaged(visible);
        }
    }
}
