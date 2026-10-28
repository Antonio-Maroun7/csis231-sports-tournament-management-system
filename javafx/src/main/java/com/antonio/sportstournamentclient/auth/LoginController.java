package com.antonio.sportstournamentclient.auth;

import com.antonio.sportstournamentclient.auth.dto.LoginResponse;
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
 * Login View: email + password -> POST /api/auth/login -> Dashboard.
 * Written exactly like the instructor's LoginController (Task on a daemon thread).
 */
public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label messageLabel;

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // client-side validation (the server validates again)
        if (email.isEmpty() || password.isEmpty()) {
            Ui.error(messageLabel, "Please fill in all the fields!");
            return;
        }
        if (!email.contains("@")) {
            Ui.error(messageLabel, "Please enter a valid email address!");
            return;
        }

        Ui.info(messageLabel, "Logging in...");
        loginButton.setDisable(true);

        // call the api in the background. prevent UI freezing.
        Task<LoginResponse> task = new Task<>() {
            @Override
            protected LoginResponse call() {
                return AuthApi.login(email, password);
            }
        };

        task.setOnSucceeded(e -> {
            Session.setCurrentUser(task.getValue());
            try {
                SceneManager.switchScene("dashboard/dashboard-view.fxml");
            } catch (IOException ex) {
                loginButton.setDisable(false);
                Ui.error(messageLabel, "Could not open the Dashboard: " + ex.getMessage());
            }
        });

        task.setOnFailed(e -> {
            loginButton.setDisable(false);
            Throwable error = task.getException();
            Ui.error(messageLabel, error instanceof ApiException ? error.getMessage() : "Login failed");
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true); // don't keep the app alive after the window closes
        thread.start();
    }

    @FXML
    private void handleRegister() throws IOException {
        SceneManager.switchScene("auth/registration-view.fxml");
    }

    @FXML
    private void handleClear() {
        emailField.clear();
        passwordField.clear();
        messageLabel.setText("");
    }
}
