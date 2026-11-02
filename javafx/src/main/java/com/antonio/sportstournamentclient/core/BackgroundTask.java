package com.antonio.sportstournamentclient.core;

import javafx.concurrent.Task;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Runs an API call off the UI thread - the same Task + daemon Thread pattern as the
 * instructor's LoginController, written once so every controller can reuse it.
 *
 * onSuccess / onError are executed back on the JavaFX thread, so they may update labels and tables.
 */
public class BackgroundTask {

    public static <T> void run(Callable<T> call, Consumer<T> onSuccess, Consumer<String> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return call.call();
            }
        };

        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> {
            Throwable error = task.getException();
            if (isExpiredSession(error)) {
                returnToLogin();
            } else {
                onError.accept(messageOf(error));
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true); // don't keep the app alive after the window closes
        thread.start();
    }

    /** A 401 while a user is logged in means the token expired (or the account was deleted). */
    private static boolean isExpiredSession(Throwable error) {
        return error instanceof ApiException api && api.getStatus() == 401 && Session.getCurrentUser() != null;
    }

    /** Forgets the session and shows the Login view with a one-shot "session expired" message. */
    private static void returnToLogin() {
        Session.clear();
        Session.setFlashMessage("Your session has expired. Please log in again.");
        try {
            SceneManager.switchScene("auth/login-view.fxml");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /** ApiException already carries the server's "detail"; anything else gets a generic message. */
    private static String messageOf(Throwable error) {
        if (error instanceof ApiException) {
            return error.getMessage();
        }
        return "Unexpected error: " + (error == null ? "unknown" : error.getMessage());
    }
}
