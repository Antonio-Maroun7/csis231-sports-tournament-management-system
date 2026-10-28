package com.antonio.sportstournamentclient.user;
import com.antonio.sportstournamentclient.core.Navigation; import javafx.fxml.FXML; import javafx.scene.Node; import javafx.scene.control.Label;
/** Controller for users.fxml. */
public final class UsersController { @FXML private Node root; @FXML private Label status; @FXML private void load(){UserApi.all().thenAccept(a->status.setText(a.length+" users loaded"));} @FXML private void dashboard(){Navigation.show(root,"../dashboard/dashboard-view.fxml");} @FXML private void teams(){Navigation.show(root,"../team/teams-view.fxml");} @FXML private void logout(){Navigation.show(root,"../auth/login-view.fxml");} }

