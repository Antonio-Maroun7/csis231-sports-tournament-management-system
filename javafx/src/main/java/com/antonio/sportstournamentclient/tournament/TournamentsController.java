package com.antonio.sportstournamentclient.tournament;

import com.antonio.sportstournamentclient.core.BackgroundTask;
import com.antonio.sportstournamentclient.core.Formats;
import com.antonio.sportstournamentclient.core.SceneManager;
import com.antonio.sportstournamentclient.core.Session;
import com.antonio.sportstournamentclient.core.Ui;
import com.antonio.sportstournamentclient.tournament.dto.TournamentRequest;
import com.antonio.sportstournamentclient.tournament.dto.TournamentResponse;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.util.List;

/**
 * Tournaments View (mockup 08): browse all tournaments (optionally filtered by status).
 * Organizers / Admins create, edit, delete and move their tournaments forward
 * (DRAFT -> OPEN -> IN_PROGRESS -> COMPLETED). Double-click a row to open the details.
 */
public class TournamentsController {
    private static final String ALL = "ALL";

    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<TournamentResponse> tournamentTable;
    @FXML private TableColumn<TournamentResponse, String> nameColumn;
    @FXML private TableColumn<TournamentResponse, String> organizerColumn;
    @FXML private TableColumn<TournamentResponse, String> startColumn;
    @FXML private TableColumn<TournamentResponse, String> endColumn;
    @FXML private TableColumn<TournamentResponse, String> statusColumn;
    @FXML private Button createButton;
    @FXML private Button editButton;
    @FXML private Button advanceButton;
    @FXML private Button deleteButton;
    @FXML private Label messageLabel;

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        organizerColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().organizerName()));
        startColumn.setCellValueFactory(c -> new SimpleStringProperty(Formats.date(c.getValue().startDate())));
        endColumn.setCellValueFactory(c -> new SimpleStringProperty(Formats.date(c.getValue().endDate())));
        statusColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status()));
        statusColumn.setCellFactory(column -> new StatusBadgeCell());

        // status filter: "All statuses", "Draft", "Open", "In Progress", "Completed"
        statusFilter.getItems().addAll(ALL, TournamentStatuses.DRAFT, TournamentStatuses.OPEN,
                TournamentStatuses.IN_PROGRESS, TournamentStatuses.COMPLETED);
        statusFilter.setCellFactory(list -> new StatusFilterCell());
        statusFilter.setButtonCell(new StatusFilterCell());
        statusFilter.setValue(ALL);
        statusFilter.valueProperty().addListener((obs, old, value) -> loadTournaments());

        // only Organizers and Admins change tournaments; Team Managers browse
        boolean canWrite = Session.canCreateTournaments();
        for (Button button : List.of(createButton, editButton, advanceButton, deleteButton)) {
            button.setVisible(canWrite);
            button.setManaged(canWrite);
        }
        tournamentTable.getSelectionModel().selectedItemProperty().addListener((obs, old, t) -> updateRowButtons(t));
        updateRowButtons(null);

        // double-click -> Tournament Details
        tournamentTable.setRowFactory(table -> {
            TableRow<TournamentResponse> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openDetails(row.getItem());
                }
            });
            return row;
        });

        loadTournaments();
    }

    @FXML
    private void loadTournaments() {
        String filter = statusFilter.getValue();
        Ui.info(messageLabel, "Loading tournaments...");
        BackgroundTask.run(
                () -> ALL.equals(filter) ? TournamentApi.getAll() : TournamentApi.getByStatus(filter),
                tournaments -> {
                    tournamentTable.getItems().setAll(tournaments);
                    int count = tournaments.size();
                    Ui.info(messageLabel, count + (count == 1 ? " tournament" : " tournaments") + " loaded");
                },
                error -> Ui.error(messageLabel, error));
    }

    @FXML
    private void handleCreate() {
        TournamentDialog.askTournament("Create Tournament", null).ifPresent(form -> {
            Long me = Session.getCurrentUser().id();
            TournamentRequest request = new TournamentRequest(form.name(), form.startDate(), form.endDate(), me);
            BackgroundTask.run(
                    () -> TournamentApi.create(request),
                    created -> {
                        loadTournaments();
                        Ui.success(messageLabel, "'" + created.name() + "' created as a draft");
                    },
                    error -> Ui.error(messageLabel, error));
        });
    }

    @FXML
    private void handleEdit() {
        TournamentResponse tournament = tournamentTable.getSelectionModel().getSelectedItem();
        if (tournament == null) {
            return;
        }
        TournamentDialog.askTournament("Edit Tournament", tournament).ifPresent(form -> {
            // the organizer (owner) does not change when editing
            TournamentRequest request = new TournamentRequest(form.name(), form.startDate(), form.endDate(),
                    tournament.organizerId());
            BackgroundTask.run(
                    () -> TournamentApi.update(tournament.id(), request),
                    updated -> {
                        loadTournaments();
                        Ui.success(messageLabel, "'" + updated.name() + "' updated");
                    },
                    error -> Ui.error(messageLabel, error));
        });
    }

    /** Moves the selected tournament one step forward: DRAFT -> OPEN -> IN_PROGRESS -> COMPLETED. */
    @FXML
    private void handleAdvance() {
        TournamentResponse tournament = tournamentTable.getSelectionModel().getSelectedItem();
        if (tournament == null) {
            return;
        }
        String next = TournamentStatuses.next(tournament.status());
        if (next == null || !Ui.confirm(TournamentStatuses.actionText(tournament.status()),
                "Move '" + tournament.name() + "' from " + TournamentStatuses.label(tournament.status())
                        + " to " + TournamentStatuses.label(next) + "?")) {
            return;
        }
        BackgroundTask.run(
                () -> TournamentApi.changeStatus(tournament.id(), next),
                updated -> {
                    loadTournaments();
                    Ui.success(messageLabel, "'" + updated.name() + "' is now " + TournamentStatuses.label(updated.status()));
                },
                // e.g. 409 when the status was already changed by someone else
                error -> Ui.error(messageLabel, error));
    }

    @FXML
    private void handleDelete() {
        TournamentResponse tournament = tournamentTable.getSelectionModel().getSelectedItem();
        if (tournament == null || !Ui.confirm("Delete Tournament", "Delete '" + tournament.name() + "'?")) {
            return;
        }
        BackgroundTask.run(
                () -> {
                    TournamentApi.delete(tournament.id());
                    return tournament;
                },
                deleted -> {
                    loadTournaments();
                    Ui.success(messageLabel, "'" + deleted.name() + "' deleted");
                },
                // e.g. 409 when teams are already registered
                error -> Ui.error(messageLabel, error));
    }

    @FXML
    private void handleOpenDetails() {
        TournamentResponse tournament = tournamentTable.getSelectionModel().getSelectedItem();
        if (tournament == null) {
            Ui.error(messageLabel, "Select a tournament first");
            return;
        }
        openDetails(tournament);
    }

    private void openDetails(TournamentResponse tournament) {
        Session.setSelectedTournamentId(tournament.id());
        try {
            SceneManager.switchScene("tournament/tournament-details-view.fxml");
        } catch (IOException e) {
            Ui.error(messageLabel, "Could not open tournament details: " + e.getMessage());
        }
    }

    /** Enables Edit / Advance / Delete only for a selected tournament the user may manage. */
    private void updateRowButtons(TournamentResponse tournament) {
        boolean manageable = tournament != null && Session.canManageTournament(tournament.organizerId());
        boolean completed = tournament != null && TournamentStatuses.COMPLETED.equals(tournament.status());

        editButton.setDisable(!manageable || completed);
        deleteButton.setDisable(!manageable);
        advanceButton.setText(tournament == null ? "Advance Status" : TournamentStatuses.actionText(tournament.status()));
        advanceButton.setDisable(!manageable || completed);
    }

    /** Table cell that shows the status as a coloured badge. */
    private static class StatusBadgeCell extends TableCell<TournamentResponse, String> {
        private final Label badge = new Label();

        @Override
        protected void updateItem(String status, boolean empty) {
            super.updateItem(status, empty);
            setText(null);
            if (empty || status == null) {
                setGraphic(null);
            } else {
                TournamentStatuses.styleBadge(badge, status);
                setGraphic(badge);
            }
        }
    }

    /** Combo box cell that shows "All statuses" / "In Progress" instead of the raw enum value. */
    private static class StatusFilterCell extends ListCell<String> {
        @Override
        protected void updateItem(String value, boolean empty) {
            super.updateItem(value, empty);
            if (empty || value == null) {
                setText(null);
            } else {
                setText(ALL.equals(value) ? "All statuses" : TournamentStatuses.label(value));
            }
        }
    }
}
