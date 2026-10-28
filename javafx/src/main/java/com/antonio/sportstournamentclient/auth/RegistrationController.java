package com.antonio.sportstournamentclient.auth;

import com.antonio.sportstournamentclient.auth.dto.RegisterRequest;
import com.antonio.sportstournamentclient.core.BackgroundTask;
import com.antonio.sportstournamentclient.core.SceneManager;
import com.antonio.sportstournamentclient.core.Session;
import com.antonio.sportstournamentclient.core.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/** Controller for account registration. */
public final class RegistrationController {
    @FXML private TextField name;
    @FXML private TextField email;
    @FXML private PasswordField password;
    @FXML private PasswordField confirmPassword;
    @FXML private Label error;
    @FXML private Button registerButton;

    @FXML
    private void register() {
        if (name.getText().isBlank() || !email.getText().contains("@")
                || password.getText().length() < 6
                || !password.getText().equals(confirmPassword.getText())) {
            error.setText("Complete all fields and ensure passwords match.");
            return;
        }
        registerButton.setDisable(true);
        var task = BackgroundTask.run(() -> AuthApi.register(
                new RegisterRequest(name.getText(), email.getText(), password.getText())));
        task.setOnSucceeded(event -> {
            Session.setCurrentUser(task.getValue());
            SceneManager.show("dashboard/dashboard-view.fxml");
        });
        task.setOnFailed(event -> {
            Ui.error(error, task.getException(), "Registration failed.");
            registerButton.setDisable(false);
        });
    }

    @FXML
    private void back() {
        SceneManager.show("auth/login-view.fxml");
    }
}
