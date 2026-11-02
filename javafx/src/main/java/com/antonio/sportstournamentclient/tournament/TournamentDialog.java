package com.antonio.sportstournamentclient.tournament;

import com.antonio.sportstournamentclient.core.Ui;
import com.antonio.sportstournamentclient.tournament.dto.TournamentResponse;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.Optional;

/**
 * "Create Tournament" / "Edit Tournament" dialog: name, start date and end date.
 * OK stays disabled until the name is 1-120 characters and the end date is not before the start date
 * (the same rules as the server).
 */
public class TournamentDialog {
    private static final int MAX_NAME_LENGTH = 120;

    /** What the dialog returns; the controller turns it into a TournamentRequest (the owner is taken from the JWT by the server). */
    public record TournamentForm(String name, LocalDate startDate, LocalDate endDate) {
    }

    /**
     * Opens the modal dialog and waits for the answer (call it on the JavaFX thread).
     *
     * @param title    window and header title, e.g. "Create Tournament"
     * @param existing the tournament to edit, or null to create a new one
     * @return the entered name and dates, or empty if the user cancelled
     */
    public static Optional<TournamentForm> askTournament(String title, TournamentResponse existing) {
        Dialog<TournamentForm> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Ui.style(dialog);

        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Autumn Cup 2026");
        DatePicker startPicker = new DatePicker();
        DatePicker endPicker = new DatePicker();
        // pick dates from the calendar only: a typed date is not committed until focus leaves the field
        startPicker.setEditable(false);
        endPicker.setEditable(false);
        Label hintLabel = new Label("The end date must be on or after the start date.");
        hintLabel.getStyleClass().add("page-subtitle");

        if (existing != null) {
            nameField.setText(existing.name());
            startPicker.setValue(existing.startDate());
            endPicker.setValue(existing.endDate());
        }

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(10, 10, 0, 10));
        form.addRow(0, new Label("Name"), nameField);
        form.addRow(1, new Label("Start date"), startPicker);
        form.addRow(2, new Label("End date"), endPicker);
        form.add(hintLabel, 0, 3, 2, 1);
        dialog.getDialogPane().setContent(form);

        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> !isValid(nameField.getText(), startPicker.getValue(), endPicker.getValue()),
                nameField.textProperty(), startPicker.valueProperty(), endPicker.valueProperty()));

        dialog.setResultConverter(button -> button == ButtonType.OK
                ? new TournamentForm(nameField.getText().trim(), startPicker.getValue(), endPicker.getValue())
                : null);

        return dialog.showAndWait();
    }

    private static boolean isValid(String name, LocalDate start, LocalDate end) {
        String trimmed = name.trim();
        return !trimmed.isEmpty()
                && trimmed.length() <= MAX_NAME_LENGTH
                && start != null
                && end != null
                && !end.isBefore(start);
    }
}
