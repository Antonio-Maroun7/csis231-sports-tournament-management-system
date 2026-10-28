package com.antonio.sportstournamentclient.team;

import com.antonio.sportstournamentclient.core.Ui;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

/**
 * Small dialog asking for a team name, used by "Create Team" and "Edit Team".
 * OK stays disabled while the name is empty or longer than the server limit.
 */
public class TeamDialog {
    private static final int MAX_NAME_LENGTH = 100;   // same as @Size(max = 100) on the server

    /** Returns the trimmed name, or empty if the user cancelled. */
    public static Optional<String> askTeamName(String title, String currentName) {
        TextInputDialog dialog = new TextInputDialog(currentName == null ? "" : currentName);
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        dialog.setContentText("Team name:");
        Ui.style(dialog);

        TextField nameField = dialog.getEditor();
        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.disableProperty().bind(Bindings.createBooleanBinding(() -> {
            String name = nameField.getText().trim();
            return name.isEmpty() || name.length() > MAX_NAME_LENGTH;
        }, nameField.textProperty()));

        return dialog.showAndWait().map(String::trim);
    }
}
