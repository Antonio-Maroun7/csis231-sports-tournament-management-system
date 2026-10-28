package com.antonio.sportstournamentclient.dashboard;
import com.antonio.sportstournamentclient.core.Navigation; import javafx.fxml.FXML; import javafx.scene.Node;
/** Controller for dashboard.fxml. */
public final class DashboardController { @FXML private Node root; @FXML private void teams(){Navigation.show(root,"../team/teams-view.fxml");} @FXML private void users(){Navigation.show(root,"../user/users-view.fxml");} @FXML private void logout(){Navigation.show(root,"../auth/login-view.fxml");} }

