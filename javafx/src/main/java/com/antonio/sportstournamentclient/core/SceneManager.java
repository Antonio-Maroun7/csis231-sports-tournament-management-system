package com.antonio.sportstournamentclient.core;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Loads views while preserving one reusable scene instance. */
public final class SceneManager {
    public static final String VIEW_ROOT = "/com/antonio/sportstournamentclient/";
    private static final String STYLESHEET = VIEW_ROOT + "styles.css";
    private static Stage stage;
    private static Scene scene;
    private static String currentView;

    private SceneManager() {
    }

    public static void initialize(Stage primaryStage) {
        stage = primaryStage;
        scene = null;
        currentView = null;
    }

    public static void setStage(Stage primaryStage) {
        initialize(primaryStage);
    }

    public static void switchScene(String view) throws IOException {
        if (stage == null) {
            throw new IllegalStateException("SceneManager has not been initialized");
        }
        if (view == null || view.isBlank()) {
            throw new IOException("View path must not be null or blank");
        }
        String resource = view.startsWith("/") ? view : VIEW_ROOT + view;
        if (SceneManager.class.getResource(resource) == null) {
            throw new IOException("View not found: " + resource);
        }
        currentView = view;
        try {
            Parent root = FXMLLoader.load(SceneManager.class.getResource(resource));
            if (scene == null) {
                scene = new Scene(root, 1180, 740);
                String stylesheet = getStylesheet();
                if (stylesheet != null) {
                    scene.getStylesheets().add(stylesheet);
                }
            } else {
                scene.setRoot(root);
            }
            stage.setScene(scene);
            stage.show();
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Unable to load " + resource, exception);
        }
    }

    public static void show(String view) {
        try {
            switchScene(view);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static String getCurrentView() {
        return currentView;
    }

    public static String currentView() {
        return getCurrentView();
    }

    public static String getStylesheet() {
        var stylesheet = SceneManager.class.getResource(STYLESHEET);
        return stylesheet == null ? null : stylesheet.toExternalForm();
    }

    public static Stage stage() {
        return stage;
    }
}
