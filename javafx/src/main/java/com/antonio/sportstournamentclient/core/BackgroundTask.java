package com.antonio.sportstournamentclient.core;

import java.util.concurrent.Callable;
import javafx.concurrent.Task;

/** Runs blocking API work without blocking the JavaFX application thread. */
public final class BackgroundTask {
    private BackgroundTask() {
    }

    public static <T> Task<T> run(Callable<T> action) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return action.call();
            }
        };
        Thread thread = new Thread(task, "api-task");
        thread.setDaemon(true);
        thread.start();
        return task;
    }
}
