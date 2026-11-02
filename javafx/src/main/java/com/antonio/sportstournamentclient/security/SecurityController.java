package com.antonio.sportstournamentclient.security;

import com.antonio.sportstournamentclient.auth.dto.LoginResponse;
import com.antonio.sportstournamentclient.core.BackgroundTask;
import com.antonio.sportstournamentclient.core.Session;
import com.antonio.sportstournamentclient.core.Ui;
import com.antonio.sportstournamentclient.user.UserApi;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Security Settings View (proposal mockup 03): turn two-factor authentication on/off.
 * The code is sent by email, so there is no QR code or backup codes.
 *
 * Administrators and Organizers always use 2FA (the server refuses to turn it off);
 * Team Managers can choose. Calls PUT /api/users/{id}/two-factor.
 */
public class SecurityController {
    @FXML private Label statusBadge;
    @FXML private Label descriptionLabel;
    @FXML private Label requiredLabel;
    @FXML private Button toggleButton;
    @FXML private Label messageLabel;

    private boolean enabled;
    private boolean required;

    @FXML
    private void initialize() {
        LoginResponse user = Session.getCurrentUser();
        required = Session.isAdmin() || Session.isOrganizer();
        descriptionLabel.setText("When it is on, after your password we email a 6-digit code to "
                + user.email() + ". The code is valid for 5 minutes and can be used only once.");
        requiredLabel.setVisible(required);
        requiredLabel.setManaged(required);
        show(user.twoFactorEnabled() || required);
    }

    @FXML
    private void handleToggle() {
        boolean turnOn = !enabled;
        if (!turnOn && !Ui.confirm("Disable 2FA",
                "Without two-factor authentication your password alone is enough to log in. Disable it?")) {
            return;
        }
        LoginResponse user = Session.getCurrentUser();
        toggleButton.setDisable(true);
        Ui.info(messageLabel, turnOn ? "Turning 2FA on..." : "Turning 2FA off...");

        BackgroundTask.run(
                () -> UserApi.updateTwoFactor(user.id(), turnOn),
                updated -> {
                    // keep the Session in sync with the server
                    Session.setCurrentUser(new LoginResponse(user.id(), user.name(), user.email(),
                            user.role(), updated.twoFactorEnabled(), user.token()));
                    show(updated.twoFactorEnabled());
                    Ui.success(messageLabel, updated.twoFactorEnabled()
                            ? "2FA is on. Next time you log in, we will email you a code."
                            : "2FA is off. You will log in with your password only.");
                },
                error -> {
                    show(enabled);
                    Ui.error(messageLabel, error);
                });
    }

    /** Badge "Enabled"/"Disabled" and the matching button (mockup 03). */
    private void show(boolean isEnabled) {
        enabled = isEnabled;
        statusBadge.setText(isEnabled ? "Enabled" : "Disabled");
        statusBadge.getStyleClass().removeAll("badge-approved", "badge-draft");
        statusBadge.getStyleClass().add(isEnabled ? "badge-approved" : "badge-draft");

        toggleButton.setText(isEnabled ? "Disable 2FA" : "Enable 2FA");
        toggleButton.getStyleClass().removeAll("primary-button", "danger-button");
        toggleButton.getStyleClass().add(isEnabled ? "danger-button" : "primary-button");
        toggleButton.setDisable(required && isEnabled);
    }
}
