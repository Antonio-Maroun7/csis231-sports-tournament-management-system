package com.antonio.sportstournamentclient.core;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

import java.util.Optional;
import java.util.function.Function;

/**
 * Small UI helpers shared by all screens: coloured status messages and confirmation dialogs.
 */
public class Ui {

    /**
     * Shows a neutral (info) message in a label.
     *
     * @param label the message label of the screen
     * @param text  the text to show
     */
    public static void info(Label label, String text) {
        setMessage(label, text, "message-info");
    }

    /**
     * Shows a green (success) message in a label.
     *
     * @param label the message label of the screen
     * @param text  the text to show
     */
    public static void success(Label label, String text) {
        setMessage(label, text, "message-success");
    }

    /**
     * Shows a red (error) message in a label.
     *
     * @param label the message label of the screen
     * @param text  the text to show, usually the server's {@code detail}
     */
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

    /** Makes a ComboBox show each item as text, e.g. Ui.displayAs(teamCombo, TeamResponse::teamName). */
    public static <T> void displayAs(ComboBox<T> comboBox, Function<T, String> toText) {
        comboBox.setCellFactory(list -> new TextCell<>(toText));
        comboBox.setButtonCell(new TextCell<>(toText));
    }

    private static class TextCell<T> extends ListCell<T> {
        private final Function<T, String> toText;

        TextCell(Function<T, String> toText) {
            this.toText = toText;
        }

        /** Displays the item as the text produced by {@code toText}; an empty cell shows nothing. */
        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null : toText.apply(item));
        }
    }

    private static void setMessage(Label label, String text, String styleClass) {
        label.getStyleClass().removeAll("message-info", "message-success", "message-error");
        label.getStyleClass().add(styleClass);
        label.setText(text);
    }
}
