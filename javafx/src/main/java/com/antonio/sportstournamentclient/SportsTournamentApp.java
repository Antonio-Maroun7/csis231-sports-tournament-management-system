package com.antonio.sportstournamentclient;

import javafx.application.Application;
import com.antonio.sportstournamentclient.core.SceneManager;
import javafx.stage.Stage;

/** JavaFX application entry point. */
public final class SportsTournamentApp extends Application {
    @Override public void start(Stage stage) throws Exception {
        stage.setTitle("Sports Tournament Management");
        SceneManager.initialize(stage);
        SceneManager.show("auth/login-view.fxml");
    }
    public static void main(String[] args) { launch(args); }
}

