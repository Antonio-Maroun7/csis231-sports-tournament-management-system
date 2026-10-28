package com.antonio.sportstournamentclient.auth;

import com.antonio.sportstournamentclient.auth.dto.RegisterResponse;
import com.antonio.sportstournamentclient.core.ApiException;
import com.antonio.sportstournamentclient.core.SceneManager;
import com.antonio.sportstournamentclient.core.Session;
import com.antonio.sportstournamentclient.core.Ui;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

/**
 * Registration View: creates a Team Manager account (POST /api/auth/register),
 * logs the new user in and opens the Dashboard - same flow as the instructor's RegistrationController.
 */
public class RegistrationController {
    private static final int MIN_PASSWORD_LENGTH = 6;   // same rule as the server (@Size(min = 6))

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Label messageLabel;

    @FXML
    private void handleRegistration() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Ui.error(messageLabel, "Please fill in all the fields!");
            return;
        }
        if (!email.contains("@")) {
            Ui.error(messageLabel, "Please enter a valid email address!");
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            Ui.error(messageLabel, "Password must be at least " + MIN_PASSWORD_LENGTH + " characters!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            Ui.error(messageLabel, "Passwords do not match!");
            return;
        }

        Ui.info(messageLabel, "Creating your account...");
        registerButton.setDisable(true);

        Task<RegisterResponse> task = new Task<>() {
            @Override
            protected RegisterResponse call() {
                return AuthApi.register(name, email, password);
            }
        };

        task.setOnSucceeded(e -> {
            // registering also logs the user in
            Session.setCurrentUser(task.getValue().toLoginResponse());
            try {
                SceneManager.switchScene("dashboard/dashboard-view.fxml");
            } catch (IOException ex) {
                registerButton.setDisable(false);
                Ui.error(messageLabel, "Could not open the Dashboard: " + ex.getMessage());
            }
        });

        task.setOnFailed(e -> {
            registerButton.setDisable(false);
            Throwable error = task.getException();
            // e.g. 409 "An account with this email already exists"
            Ui.error(messageLabel, error instanceof ApiException ? error.getMessage() : "Registration failed");
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleLogin() throws IOException {
        SceneManager.switchScene("auth/login-view.fxml");
    }
}
