package com.antonio.sportstournamentclient.core;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import java.io.IOException;

/** Owns application scene transitions and keeps FXML loading consistent. */
public final class SceneManager {
    private static Stage stage;
    private SceneManager() {}
    public static void initialize(Stage primaryStage) { stage = primaryStage; }
    public static void setStage(Stage primaryStage) { initialize(primaryStage); }
    /** Alias retained for controllers that use an explicit stage transition. */
    public static void switchScene(String resource) { show(resource); }
    public static void show(String resource) {
        try {
            Parent root = FXMLLoader.load(SceneManager.class.getResource(resource));
            Scene scene = stage.getScene();
            if (scene == null) scene = new Scene(root, 1000, 700);
            else scene.setRoot(root);
            var css = SceneManager.class.getResource("/com/antonio/sportstournamentclient/styles.css");
            if (css != null && !scene.getStylesheets().contains(css.toExternalForm()))
                scene.getStylesheets().add(css.toExternalForm());
            stage.setScene(scene); stage.show();
        } catch (IOException | RuntimeException e) { throw new IllegalStateException("Unable to load " + resource, e); }
    }
    public static Stage stage() { return stage; }
}
