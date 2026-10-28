package com.antonio.sportstournamentclient.user;

import com.antonio.sportstournamentclient.core.Ui;
import com.antonio.sportstournamentclient.user.dto.UserCreateRequest;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.Optional;

/**
 * "Create User" dialog (Administrator): name, email, password and role.
 * OK stays disabled until every field is valid (same limits as the server).
 */
public class UserDialog {
    private static final int MIN_PASSWORD_LENGTH = 6;

    public static Optional<UserCreateRequest> askNewUser() {
        Dialog<UserCreateRequest> dialog = new Dialog<>();
        dialog.setTitle("Create User");
        dialog.setHeaderText("Create a new account");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Ui.style(dialog);

        TextField nameField = new TextField();
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("At least " + MIN_PASSWORD_LENGTH + " characters");
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("TEAM_MANAGER", "ORGANIZER", "ADMIN");
        roleBox.setValue("TEAM_MANAGER");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(10, 10, 0, 10));
        form.addRow(0, new Label("Name"), nameField);
        form.addRow(1, new Label("Email"), emailField);
        form.addRow(2, new Label("Password"), passwordField);
        form.addRow(3, new Label("Role"), roleBox);
        dialog.getDialogPane().setContent(form);

        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> nameField.getText().isBlank()
                        || !emailField.getText().contains("@")
                        || passwordField.getText().length() < MIN_PASSWORD_LENGTH
                        || roleBox.getValue() == null,
                nameField.textProperty(), emailField.textProperty(),
                passwordField.textProperty(), roleBox.valueProperty()));

        dialog.setResultConverter(button -> button == ButtonType.OK
                ? new UserCreateRequest(nameField.getText().trim(), emailField.getText().trim(),
                        passwordField.getText(), roleBox.getValue())
                : null);

        return dialog.showAndWait();
    }
}
