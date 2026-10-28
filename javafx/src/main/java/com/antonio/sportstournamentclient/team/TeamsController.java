package com.antonio.sportstournamentclient.team;
import com.antonio.sportstournamentclient.core.Navigation; import javafx.fxml.FXML; import javafx.scene.Node; import javafx.scene.control.Label;
/** Controller for teams.fxml. */
public final class TeamsController { @FXML private Node root; @FXML private Label status; @FXML private void load(){TeamApi.all().thenAccept(a->status.setText(a.length+" teams loaded"));} @FXML private void dashboard(){Navigation.show(root,"../dashboard/dashboard-view.fxml");} @FXML private void users(){Navigation.show(root,"../user/users-view.fxml");} @FXML private void logout(){Navigation.show(root,"../auth/login-view.fxml");} }

