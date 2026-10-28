package com.antonio.sportstournamentclient;

import com.antonio.sportstournamentclient.core.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX application: registers the primary stage in the SceneManager and opens the login view.
 */
public class SportsTournamentApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.setStage(stage);
        SceneManager.switchScene("auth/login-view.fxml");
        stage.setTitle("Sports Tournament Management System");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.show();
    }
}
