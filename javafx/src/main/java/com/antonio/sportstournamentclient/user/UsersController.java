package com.antonio.sportstournamentclient.user;

import com.antonio.sportstournamentclient.core.BackgroundTask;
import com.antonio.sportstournamentclient.core.Formats;
import com.antonio.sportstournamentclient.core.SceneManager;
import com.antonio.sportstournamentclient.core.Session;
import com.antonio.sportstournamentclient.core.Ui;
import com.antonio.sportstournamentclient.user.dto.UserResponse;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;

/**
 * Users View (Administrator only): list all accounts, create an account with any role, delete an account.
 */
public class UsersController {
    @FXML private TableView<UserResponse> userTable;
    @FXML private TableColumn<UserResponse, String> nameColumn;
    @FXML private TableColumn<UserResponse, String> emailColumn;
    @FXML private TableColumn<UserResponse, String> roleColumn;
    @FXML private TableColumn<UserResponse, String> twoFactorColumn;
    @FXML private TableColumn<UserResponse, String> createdColumn;
    @FXML private Button createButton;
    @FXML private Button deleteButton;
    @FXML private Label messageLabel;

    @FXML
    private void initialize() {
        // guard: the sidebar hides this page, but never trust the client alone
        if (!Session.isAdmin()) {
            Ui.error(messageLabel, "Access denied - administrators only");
            createButton.setDisable(true);
            deleteButton.setDisable(true);
            // leave the page after it is shown
            Platform.runLater(this::goToDashboard);
            return;
        }

        nameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        emailColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().email()));
        roleColumn.setCellValueFactory(c -> new SimpleStringProperty(Formats.role(c.getValue().role())));
        twoFactorColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().twoFactorEnabled() ? "Enabled" : "Off"));
        createdColumn.setCellValueFactory(c -> new SimpleStringProperty(Formats.date(c.getValue().createdAt())));

        // cannot delete without a selection, and never your own account
        deleteButton.setDisable(true);
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, old, user) ->
                deleteButton.setDisable(user == null || user.id().equals(Session.getCurrentUser().id())));

        loadUsers();
    }

    @FXML
    private void loadUsers() {
        Ui.info(messageLabel, "Loading users...");
        BackgroundTask.run(
                UserApi::getAll,
                users -> {
                    userTable.getItems().setAll(users);
                    Ui.info(messageLabel, users.size() + (users.size() == 1 ? " user loaded" : " users loaded"));
                },
                error -> Ui.error(messageLabel, error));
    }

    @FXML
    private void handleCreate() {
        UserDialog.askNewUser().ifPresent(request ->
                BackgroundTask.run(
                        () -> UserApi.create(request),
                        created -> {
                            Ui.success(messageLabel, "Account created for " + created.email());
                            loadUsers();
                        },
                        // e.g. 409 "An account with this email already exists"
                        error -> Ui.error(messageLabel, error)));
    }

    @FXML
    private void handleDelete() {
        UserResponse user = userTable.getSelectionModel().getSelectedItem();
        if (user == null || !Ui.confirm("Delete User", "Delete the account " + user.email() + "?")) {
            return;
        }
        BackgroundTask.run(
                () -> {
                    UserApi.delete(user.id());
                    return user;
                },
                deleted -> {
                    Ui.success(messageLabel, "Account " + deleted.email() + " deleted");
                    loadUsers();
                },
                // e.g. 409 when the user still manages a team
                error -> Ui.error(messageLabel, error));
    }

    private void goToDashboard() {
        try {
            SceneManager.switchScene("dashboard/dashboard-view.fxml");
        } catch (IOException e) {
            Ui.error(messageLabel, "Could not open the Dashboard: " + e.getMessage());
        }
    }
}
