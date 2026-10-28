package com.antonio.sportstournamentclient.auth;

import com.antonio.sportstournamentclient.auth.dto.RegisterRequest;
import com.antonio.sportstournamentclient.core.ApiClient;
import com.antonio.sportstournamentclient.core.ApiException;
import com.antonio.sportstournamentclient.core.Navigation;
import com.antonio.sportstournamentclient.core.Session;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

/** Registration controller with confirmation validation and observable API task state. */
public final class RegistrationController {
    @FXML private Node root;
    @FXML private TextField name;
    @FXML private TextField email;
    @FXML private PasswordField password;
    @FXML private PasswordField confirmPassword;
    @FXML private Label error;
    @FXML private Button registerButton;

    @FXML private void register() {
        if (name.getText().isBlank() || !email.getText().contains("@")
                || password.getText().length() < 6
                || !password.getText().equals(confirmPassword.getText())) {
            error.setText("Complete all fields and ensure passwords match.");
            return;
        }
        registerButton.setDisable(true);
        Task<com.antonio.sportstournamentclient.user.dto.UserResponse> task =
                ApiClient.task("POST", "/auth/register",
                        new RegisterRequest(name.getText(), email.getText(), password.getText()),
                        com.antonio.sportstournamentclient.user.dto.UserResponse.class);
        task.setOnSucceeded(e -> {
            Session.setCurrentUser(task.getValue());
            Platform.runLater(() -> Navigation.show(root, "../dashboard/dashboard-view.fxml"));
        });
        task.setOnFailed(e -> {
            Throwable cause = task.getException();
            error.setText(cause instanceof ApiException ? cause.getMessage() : "Registration failed.");
            registerButton.setDisable(false);
        });
    }
    @FXML private void back() { Navigation.show(root, "login-view.fxml"); }
}
