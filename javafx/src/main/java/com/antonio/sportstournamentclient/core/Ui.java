package com.antonio.sportstournamentclient.core;

import javafx.scene.control.Label;

/** Small JavaFX presentation helpers shared by controllers. */
public final class Ui {
    private Ui() {
    }

    public static void error(Label label, Throwable cause, String fallback) {
        Throwable error = cause instanceof java.util.concurrent.CompletionException
                ? cause.getCause() : cause;
        label.setText(error == null || error.getMessage() == null ? fallback : error.getMessage());
    }
}
