package com.antonio.sportstournamentclient.auth;

import com.antonio.sportstournamentclient.auth.dto.LoginRequest;
import com.antonio.sportstournamentclient.core.BackgroundTask;
import com.antonio.sportstournamentclient.core.SceneManager;
import com.antonio.sportstournamentclient.core.Session;
import com.antonio.sportstournamentclient.core.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/** Controller for the sign-in view. */
public final class LoginController {
    @FXML private TextField email;
    @FXML private PasswordField password;
    @FXML private Label error;

    @FXML
    private void login() {
        if (!email.getText().contains("@") || password.getText().length() < 6) {
            error.setText("Enter a valid email and password.");
            return;
        }
        var task = BackgroundTask.run(() -> AuthApi.login(new LoginRequest(email.getText(), password.getText())));
        task.setOnSucceeded(event -> {
            Session.setCurrentUser(task.getValue());
            SceneManager.show("dashboard/dashboard-view.fxml");
        });
        task.setOnFailed(event -> Ui.error(error, task.getException(), "Login failed."));
    }

    @FXML
    private void register() {
        SceneManager.show("auth/registration-view.fxml");
    }
}
