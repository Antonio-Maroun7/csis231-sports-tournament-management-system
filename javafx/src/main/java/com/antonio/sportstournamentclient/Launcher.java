package com.antonio.sportstournamentclient;

import javafx.application.Application;

/**
 * Entry point used by IntelliJ / the jar. It does not extend Application, which avoids the
 * "JavaFX runtime components are missing" error (same trick as the instructor's Launcher).
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(SportsTournamentApp.class, args);
    }
}
