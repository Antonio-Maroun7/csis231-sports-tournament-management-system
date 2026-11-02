package com.antonio.sportstournamentclient;

import com.antonio.sportstournamentclient.core.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX application: registers the primary stage in the SceneManager and opens the login view.
 */
public class SportsTournamentApp extends Application {
    /**
     * JavaFX entry point: gives the primary stage to {@link SceneManager}, opens the login view
     * and shows the window (minimum size 1000x650).
     *
     * @param stage the primary stage created by the JavaFX runtime
     * @throws IOException if {@code auth/login-view.fxml} cannot be loaded
     */
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
