package com.antonio.sportstournamentclient.auth;
import com.antonio.sportstournamentclient.auth.dto.*; import com.antonio.sportstournamentclient.core.*; import javafx.application.Platform; import javafx.concurrent.Task; import javafx.fxml.FXML; import javafx.scene.Node; import javafx.scene.control.*;
/** Controller for login.fxml with validation and asynchronous API access. */
public final class LoginController {
 @FXML private Node root; @FXML private TextField email; @FXML private PasswordField password; @FXML private Label error;
 @FXML private void login(){if(!email.getText().contains("@")||password.getText().length()<6){error.setText("Enter a valid email and password.");return;} Task<com.antonio.sportstournamentclient.auth.dto.LoginResponse> task=ApiClient.task("POST","/auth/login",new LoginRequest(email.getText(),password.getText()),com.antonio.sportstournamentclient.auth.dto.LoginResponse.class);task.setOnSucceeded(e->{Session.setCurrentUser(task.getValue());Navigation.show(root,"../dashboard/dashboard-view.fxml");});task.setOnFailed(e->{Throwable x=task.getException();error.setText(x instanceof ApiException?x.getMessage():"Login failed.");});}
 @FXML private void register(){Navigation.show(root,"registration-view.fxml");}
}
