package com.antonio.sportstournamentclient.core;

import javafx.concurrent.Task;

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
        task.setOnFailed(e -> onError.accept(messageOf(task.getException())));

        Thread thread = new Thread(task);
        thread.setDaemon(true); // don't keep the app alive after the window closes
        thread.start();
    }

    /** ApiException already carries the server's "detail"; anything else gets a generic message. */
    private static String messageOf(Throwable error) {
        if (error instanceof ApiException) {
            return error.getMessage();
        }
        return "Unexpected error: " + (error == null ? "unknown" : error.getMessage());
    }
}
