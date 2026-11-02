package com.antonio.sportstournamentclient.auth;

import com.antonio.sportstournamentclient.auth.dto.AuthResult;
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
 * Login View: email + password -> POST /api/auth/login.
 * Without 2FA -> Dashboard. With 2FA (always for Admins/Organizers) -> Two-Factor view,
 * where the user types the 6-digit code that was emailed to them.
 * Written exactly like the instructor's LoginController (Task on a daemon thread).
 */
public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label messageLabel;

    /** Loads the login view: shows the one-shot message left in the Session (e.g. "session expired"), if any. */
    @FXML
    private void initialize() {
        // e.g. "Your session has expired" after a 401; shown once
        String flash = Session.consumeFlashMessage();
        if (flash != null) {
            Ui.info(messageLabel, flash);
        }
    }

    /** "Login" button (or Enter): validates the fields, then calls POST /api/auth/login on a background thread; goes to the 2FA view or the dashboard. */
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
        Task<AuthResult> task = new Task<>() {
            @Override
            protected AuthResult call() {
                return AuthApi.login(email, password);
            }
        };

        task.setOnSucceeded(e -> {
            AuthResult result = task.getValue();
            try {
                if (result.twoFactorRequired()) {
                    // a 6-digit code was emailed: ask for it on the Two-Factor view
                    Session.setPendingChallenge(result);
                    SceneManager.switchScene("auth/two-factor-view.fxml");
                } else {
                    Session.setCurrentUser(result.user());
                    SceneManager.switchScene("dashboard/dashboard-view.fxml");
                }
            } catch (IOException ex) {
                loginButton.setDisable(false);
                Ui.error(messageLabel, "Could not open the next screen: " + ex.getMessage());
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

    /** "Create an account" link: opens the registration view. */
    @FXML
    private void handleRegister() throws IOException {
        SceneManager.switchScene("auth/registration-view.fxml");
    }

    /** "Clear" button: empties the email and password fields and the message. */
    @FXML
    private void handleClear() {
        emailField.clear();
        passwordField.clear();
        messageLabel.setText("");
    }
}
