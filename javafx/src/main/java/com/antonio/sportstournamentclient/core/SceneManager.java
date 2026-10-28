package com.antonio.sportstournamentclient.core;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Swaps the content of the single application window (same role as the instructor's SceneManager).
 *
 * Views are addressed relative to the client's resource root, grouped by feature:
 * {@code switchScene("team/teams-view.fxml")} loads
 * {@code /com/antonio/sportstournamentclient/team/teams-view.fxml}.
 */
public class SceneManager {
    private static final String VIEW_ROOT = "/com/antonio/sportstournamentclient/";
    private static final String STYLESHEET = VIEW_ROOT + "styles.css";

    private static Stage stage;
    private static String currentView;

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    /** The view currently shown, e.g. "team/teams-view.fxml" (used by the sidebar to highlight its item). */
    public static String getCurrentView() {
        return currentView;
    }

    /** URL of the shared stylesheet, so dialogs (separate windows) can use the same look. */
    public static String getStylesheet() {
        URL css = SceneManager.class.getResource(STYLESHEET);
        return css == null ? null : css.toExternalForm();
    }

    public static void switchScene(String fxml) throws IOException {
        // an ABSOLUTE path: a relative one would be resolved from this class's package (core/)
        URL location = SceneManager.class.getResource(VIEW_ROOT + fxml);
        if (location == null) {
            throw new IOException("View not found: " + VIEW_ROOT + fxml);
        }

        // set before loading, so controllers created by this load (e.g. the sidebar) can read it
        currentView = fxml;
        Parent root = new FXMLLoader(location).load();

        Scene scene = stage.getScene();
        if (scene == null) {
            // first view: create the scene once and attach the shared stylesheet
            scene = new Scene(root, 1180, 740);
            String css = getStylesheet();
            if (css != null) {
                scene.getStylesheets().add(css);
            }
            stage.setScene(scene);
        } else {
            // next views: keep the same scene (window size and stylesheet stay)
            scene.setRoot(root);
        }
    }
}
