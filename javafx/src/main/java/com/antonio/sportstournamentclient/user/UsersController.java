package com.antonio.sportstournamentclient.user;

import com.antonio.sportstournamentclient.core.*;
import com.antonio.sportstournamentclient.user.dto.*;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/** Full user administration screen with role, permissions, and account safety. */
public final class UsersController {
    @FXML private TableView<UserResponse> users;
    @FXML private TableColumn<UserResponse,String> nameColumn,emailColumn,roleColumn,twoFactorColumn,dateColumn,idColumn;
    @FXML private Label status;
    private final ObservableList<UserResponse> items = FXCollections.observableArrayList();

    @FXML private void initialize() {
        users.setItems(items);
        nameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        emailColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().email()));
        roleColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().role()));
        twoFactorColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().twoFactorEnabled() ? "Enabled" : "Disabled"));
        dateColumn.setCellValueFactory(c -> new SimpleStringProperty(Formats.dateTime(c.getValue().createdAt())));
        idColumn.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().id())));
        load();
    }
    @FXML private void load() {
        var task = BackgroundTask.run(UserApi::all);
        task.setOnSucceeded(e -> { List<UserResponse> values = task.getValue(); items.setAll(values); status.setText(values.size() + " users"); });
        task.setOnFailed(e -> Ui.error(status, task.getException(), "Unable to load users."));
    }
    @FXML private void create() { editDialog(null); }
    @FXML private void edit() { UserResponse u=users.getSelectionModel().getSelectedItem(); if(u==null) status.setText("Select a user."); else editDialog(u); }
    @FXML private void permissions() { UserResponse u=users.getSelectionModel().getSelectedItem(); if(u==null) status.setText("Select a user."); else new Alert(Alert.AlertType.INFORMATION, "Role permissions for " + u.name() + ": " + u.role()).showAndWait(); }
    @FXML private void delete() {
        UserResponse u=users.getSelectionModel().getSelectedItem();
        if(u==null){status.setText("Select a user.");return;}
        if(Session.currentUser()!=null && Session.currentUser().id().equals(u.id())){status.setText("You cannot delete your own account.");return;}
        new Alert(Alert.AlertType.CONFIRMATION,"Delete "+u.name()+"?",ButtonType.CANCEL,ButtonType.OK).showAndWait().filter(ButtonType.OK::equals).ifPresent(x->{var t=BackgroundTask.run(()->{UserApi.delete(u.id());return null;});t.setOnSucceeded(e->load());t.setOnFailed(e->Ui.error(status,t.getException(),"Unable to delete user."));});
    }
    private void editDialog(UserResponse existing) {
        TextField name=new TextField(existing==null?"":existing.name()), email=new TextField(existing==null?"":existing.email());
        PasswordField password=new PasswordField(); ComboBox<String> role=new ComboBox<>(FXCollections.observableArrayList("USER","ORGANIZER","TEAM_MANAGER","ADMIN")); role.setValue(existing==null?"USER":existing.role());
        CheckBox twoFactor=new CheckBox("Two-factor enabled"); if(existing!=null) twoFactor.setSelected(existing.twoFactorEnabled());
        GridPane form=new GridPane();form.setHgap(10);form.setVgap(10);form.addRow(0,new Label("Name"),name);form.addRow(1,new Label("Email"),email);form.addRow(2,new Label("Password"),password);form.addRow(3,new Label("Role"),role);form.add(twoFactor,1,4);
        Dialog<ButtonType> d=new Dialog<>();d.setTitle(existing==null?"Create user":"Edit user");d.getDialogPane().setContent(form);d.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL,ButtonType.OK);
        d.showAndWait().filter(ButtonType.OK::equals).ifPresent(x->{if(name.getText().isBlank()||email.getText().isBlank()||(existing==null&&password.getText().isBlank())){status.setText("Name, email, and password are required.");return;} var request=new UserCreateRequest(name.getText(),email.getText(),password.getText(),role.getValue()); var t=BackgroundTask.run(()->existing==null?UserApi.create(request):UserApi.update(existing.id(),request));t.setOnSucceeded(e->load());t.setOnFailed(e->Ui.error(status,t.getException(),"Unable to save user."));});
    }
}
