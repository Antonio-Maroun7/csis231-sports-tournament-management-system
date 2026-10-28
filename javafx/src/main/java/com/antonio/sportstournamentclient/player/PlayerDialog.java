package com.antonio.sportstournamentclient.player;

import com.antonio.sportstournamentclient.core.Ui;
import com.antonio.sportstournamentclient.player.dto.PlayerRequest;
import com.antonio.sportstournamentclient.player.dto.PlayerResponse;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.Optional;

/**
 * "Add Player" / "Edit Player" dialog: first name, last name, jersey number (0-99) and position.
 * OK stays disabled until the input respects the same limits as the server.
 */
public class PlayerDialog {
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_POSITION_LENGTH = 30;

    /**
     * @param existing the player to edit, or null to add a new one
     * @return the request to send, or empty if the user cancelled
     */
    public static Optional<PlayerRequest> askPlayer(String title, PlayerResponse existing) {
        Dialog<PlayerRequest> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Ui.style(dialog);

        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        Spinner<Integer> jerseySpinner = new Spinner<>(0, 99, 1);
        jerseySpinner.setEditable(true);
        ComboBox<String> positionBox = new ComboBox<>();
        positionBox.getItems().addAll("Goalkeeper", "Defender", "Midfielder", "Forward");
        positionBox.setEditable(true);   // other sports: the user may type any position

        if (existing != null) {
            firstNameField.setText(existing.firstName());
            lastNameField.setText(existing.lastName());
            jerseySpinner.getValueFactory().setValue(existing.jerseyNumber());
            positionBox.getEditor().setText(existing.position() == null ? "" : existing.position());
        }

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(10, 10, 0, 10));
        form.addRow(0, new Label("First name"), firstNameField);
        form.addRow(1, new Label("Last name"), lastNameField);
        form.addRow(2, new Label("Jersey #"), jerseySpinner);
        form.addRow(3, new Label("Position"), positionBox);
        dialog.getDialogPane().setContent(form);

        // validate what is TYPED (an editable Spinner/ComboBox only commits its value on focus loss)
        TextField jerseyEditor = jerseySpinner.getEditor();
        TextField positionEditor = positionBox.getEditor();
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> !validName(firstNameField.getText())
                        || !validName(lastNameField.getText())
                        || parseJersey(jerseyEditor.getText()) == null
                        || positionEditor.getText().trim().length() > MAX_POSITION_LENGTH,
                firstNameField.textProperty(), lastNameField.textProperty(),
                jerseyEditor.textProperty(), positionEditor.textProperty()));

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            String position = positionEditor.getText().trim();
            return new PlayerRequest(
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    parseJersey(jerseyEditor.getText()),
                    position.isEmpty() ? null : position);
        });

        return dialog.showAndWait();
    }

    private static boolean validName(String name) {
        String trimmed = name.trim();
        return !trimmed.isEmpty() && trimmed.length() <= MAX_NAME_LENGTH;
    }

    /** Returns the jersey number 0-99, or null if the text is not a valid number. */
    private static Integer parseJersey(String text) {
        String trimmed = text.trim();
        if (!trimmed.matches("\\d{1,2}")) {
            return null;
        }
        return Integer.parseInt(trimmed);
    }
}
