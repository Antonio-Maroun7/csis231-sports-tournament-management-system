package com.antonio.sportstournamentclient.auth;

import com.antonio.sportstournamentclient.auth.dto.AuthResult;
import com.antonio.sportstournamentclient.core.BackgroundTask;
import com.antonio.sportstournamentclient.core.SceneManager;
import com.antonio.sportstournamentclient.core.Session;
import com.antonio.sportstournamentclient.core.Ui;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Two-Factor Authentication view (proposal mockup 02). The 6-digit code is EMAILED by the
 * server after a correct email + password; the user types it here:
 *   Verify Code  -> POST /api/auth/verify-2fa -> Dashboard
 *   Resend code  -> POST /api/auth/resend-2fa (allowed after 60 seconds, gives a new token)
 *
 * The code is valid for 5 minutes (countdown). After 5 wrong codes, or when the code has
 * expired, the server asks the user to log in again and this view returns to the Login view.
 */
public class TwoFactorController {
    private static final int CODE_LENGTH = 6;
    private static final int RESEND_COOLDOWN_SECONDS = 60;   // same value as the server

    @FXML private Label subtitleLabel;
    @FXML private HBox codeBox;
    @FXML private Button verifyButton;
    @FXML private Hyperlink resendLink;
    @FXML private Label expiryLabel;
    @FXML private Label messageLabel;

    private final List<TextField> digitFields = new ArrayList<>();
    private final Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
    private AuthResult challenge;
    private LocalDateTime resendAllowedAt;
    private boolean busy;   // a request is running

    /** Builds the six digit boxes and starts the countdown for the pending 2FA login; returns to the login view if there is none. */
    @FXML
    private void initialize() {
        createDigitFields();
        challenge = Session.getPendingChallenge();
        if (challenge == null) {   // opened without logging in first
            Platform.runLater(this::backToLogin);
            return;
        }
        showChallenge(challenge);
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
        Platform.runLater(() -> digitFields.get(0).requestFocus());
    }

    /** "Verify Code" button: sends the six digits to POST /api/auth/verify-2fa in the background; on success the user is logged in and the dashboard opens. */
    @FXML
    private void handleVerify() {
        String code = enteredCode();
        if (busy || code.length() < CODE_LENGTH) {
            return;
        }
        setBusy(true);
        Ui.info(messageLabel, "Checking the code...");

        BackgroundTask.run(
                () -> AuthApi.verifyTwoFactor(challenge.challengeToken(), code),
                user -> {
                    clock.stop();
                    Session.setPendingChallenge(null);
                    Session.setCurrentUser(user);
                    open("dashboard/dashboard-view.fxml");
                },
                error -> {
                    setBusy(false);
                    Ui.error(messageLabel, error);
                    clearDigits();
                    if (mustLogInAgain(error)) {
                        returnToLoginSoon();
                    }
                });
    }

    /** "Resend code" link: asks POST /api/auth/resend-2fa for a new emailed code (new token and expiry). */
    @FXML
    private void handleResend() {
        if (busy) {
            return;
        }
        setBusy(true);
        Ui.info(messageLabel, "Sending a new code...");

        BackgroundTask.run(
                () -> AuthApi.resendCode(challenge.challengeToken()),
                result -> {
                    setBusy(false);
                    clearDigits();
                    showChallenge(result);   // new token + new expiry
                    Ui.success(messageLabel, "A new code was sent to " + result.maskedEmail() + ".");
                },
                error -> {
                    setBusy(false);
                    Ui.error(messageLabel, error);
                    if (mustLogInAgain(error)) {
                        returnToLoginSoon();
                    }
                });
    }

    /** "Back to login" link: drops the pending 2FA login and opens the login view. */
    @FXML
    private void handleBackToLogin() {
        backToLogin();
    }

    // -------- the six digit boxes --------

    private void createDigitFields() {
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = i;
            TextField field = new TextField();
            field.getStyleClass().add("code-digit");
            field.setTextFormatter(new TextFormatter<String>(change -> filterDigit(index, change)));

            // a digit was typed: jump to the next box
            field.textProperty().addListener((obs, oldText, text) -> {
                if (text.length() == 1 && index < CODE_LENGTH - 1) {
                    digitFields.get(index + 1).requestFocus();
                }
                updateVerifyButton();
            });
            // entering a box selects its digit, so typing replaces it
            field.focusedProperty().addListener((obs, wasFocused, focused) -> {
                if (focused) {
                    Platform.runLater(field::selectAll);
                }
            });
            field.setOnKeyPressed(event -> handleDigitKey(index, event));
            digitFields.add(field);
        }
        codeBox.getChildren().setAll(digitFields);
    }

    /** Each box accepts one digit. Pasting "482913" (or typing over a full box) fills the boxes. */
    private TextFormatter.Change filterDigit(int index, TextFormatter.Change change) {
        if (change.getControlNewText().matches("\\d?")) {
            return change;
        }
        String digits = change.getText().replaceAll("\\D", "");
        if (!digits.isEmpty()) {
            Platform.runLater(() -> fillFrom(index, digits));
        }
        return null;   // letters, spaces... are ignored
    }

    private void fillFrom(int index, String digits) {
        int position = index;
        for (char digit : digits.toCharArray()) {
            if (position >= CODE_LENGTH) {
                break;
            }
            digitFields.get(position++).setText(String.valueOf(digit));
        }
        digitFields.get(Math.min(position, CODE_LENGTH - 1)).requestFocus();
    }

    // Backspace on an empty box goes back; arrow keys move between the boxes
    private void handleDigitKey(int index, KeyEvent event) {
        switch (event.getCode()) {
            case BACK_SPACE -> {
                if (digitFields.get(index).getText().isEmpty() && index > 0) {
                    TextField previous = digitFields.get(index - 1);
                    previous.clear();
                    previous.requestFocus();
                    event.consume();
                }
            }
            case LEFT -> {
                if (index > 0) {
                    digitFields.get(index - 1).requestFocus();
                }
            }
            case RIGHT -> {
                if (index < CODE_LENGTH - 1) {
                    digitFields.get(index + 1).requestFocus();
                }
            }
            default -> {
            }
        }
    }

    private String enteredCode() {
        StringBuilder code = new StringBuilder();
        digitFields.forEach(field -> code.append(field.getText()));
        return code.toString();
    }

    private void clearDigits() {
        digitFields.forEach(TextField::clear);
        digitFields.get(0).requestFocus();
    }

    // -------- countdowns --------

    private void showChallenge(AuthResult result) {
        challenge = result;
        Session.setPendingChallenge(result);
        subtitleLabel.setText("Enter the 6-digit code we sent to " + result.maskedEmail() + ".");
        resendAllowedAt = LocalDateTime.now().plusSeconds(RESEND_COOLDOWN_SECONDS);
        tick();
    }

    /** Runs every second: code expiry and "Resend code in 0:42". */
    private void tick() {
        LocalDateTime now = LocalDateTime.now();
        long secondsLeft = challenge.codeExpiresAt() == null
                ? 1 : ChronoUnit.SECONDS.between(now, challenge.codeExpiresAt());
        boolean expired = secondsLeft <= 0;
        expiryLabel.setText(expired
                ? "The code has expired. Please log in again."
                : "The code expires in " + minutesSeconds(secondsLeft) + ".");

        long waitSeconds = ChronoUnit.SECONDS.between(now, resendAllowedAt);
        resendLink.setText(waitSeconds > 0 ? "Resend code in " + minutesSeconds(waitSeconds) : "Resend code");
        resendLink.setDisable(busy || expired || waitSeconds > 0);
        updateVerifyButton();
    }

    private void updateVerifyButton() {
        boolean expired = challenge == null || (challenge.codeExpiresAt() != null
                && !challenge.codeExpiresAt().isAfter(LocalDateTime.now()));
        verifyButton.setDisable(busy || expired || enteredCode().length() < CODE_LENGTH);
    }

    private static String minutesSeconds(long seconds) {
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }

    // -------- helpers --------

    private void setBusy(boolean value) {
        busy = value;
        if (challenge != null) {
            tick();
        }
    }

    // the server says "... Please log in again." when the code expired or was locked
    private static boolean mustLogInAgain(String error) {
        return error != null && error.contains("log in again");
    }

    private void returnToLoginSoon() {
        clock.stop();
        busy = true;
        verifyButton.setDisable(true);
        resendLink.setDisable(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
        pause.setOnFinished(e -> backToLogin());
        pause.play();
    }

    private void backToLogin() {
        clock.stop();
        Session.setPendingChallenge(null);
        open("auth/login-view.fxml");
    }

    private void open(String view) {
        try {
            SceneManager.switchScene(view);
        } catch (IOException e) {
            Ui.error(messageLabel, "Could not open the next screen: " + e.getMessage());
        }
    }
}
