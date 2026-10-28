package com.antonio.sportstournamentclient.core;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;

import java.util.Optional;

/**
 * Small UI helpers shared by all screens: coloured status messages and confirmation dialogs.
 */
public class Ui {

    public static void info(Label label, String text) {
        setMessage(label, text, "message-info");
    }

    public static void success(Label label, String text) {
        setMessage(label, text, "message-success");
    }

    public static void error(Label label, String text) {
        setMessage(label, text, "message-error");
    }

    /** Asks "are you sure?"; returns true only when the user clicks OK. */
    public static boolean confirm(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title);
        alert.setHeaderText(null);
        style(alert);
        Optional<ButtonType> answer = alert.showAndWait();
        return answer.isPresent() && answer.get() == ButtonType.OK;
    }

    /** Dialogs open in their own window: give them the application stylesheet too. */
    public static void style(Dialog<?> dialog) {
        String css = SceneManager.getStylesheet();
        if (css != null) {
            dialog.getDialogPane().getStylesheets().add(css);
        }
    }

    private static void setMessage(Label label, String text, String styleClass) {
        label.getStyleClass().removeAll("message-info", "message-success", "message-error");
        label.getStyleClass().add(styleClass);
        label.setText(text);
    }
}
