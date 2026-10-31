package com.antonio.sportstournamentclient.tournament;

import javafx.scene.control.Label;

/**
 * Everything the screens need to know about the tournament lifecycle
 * DRAFT -> OPEN -> IN_PROGRESS -> COMPLETED (same order as the server's enum).
 */
public class TournamentStatuses {
    public static final String DRAFT = "DRAFT";
    public static final String OPEN = "OPEN";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String COMPLETED = "COMPLETED";

    /** The only status the server accepts next, or null when the tournament is completed. */
    public static String next(String status) {
        return switch (status) {
            case DRAFT -> OPEN;
            case OPEN -> IN_PROGRESS;
            case IN_PROGRESS -> COMPLETED;
            default -> null;
        };
    }

    /** Text of the button that moves the tournament forward. */
    public static String actionText(String status) {
        return switch (status) {
            case DRAFT -> "Open Registration";
            case OPEN -> "Start Tournament";
            case IN_PROGRESS -> "Complete Tournament";
            default -> "Completed";
        };
    }

    /** IN_PROGRESS -> "In Progress" */
    public static String label(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case DRAFT -> "Draft";
            case OPEN -> "Open";
            case IN_PROGRESS -> "In Progress";
            case COMPLETED -> "Completed";
            default -> status;
        };
    }

    /** Turns a Label into a coloured status badge (see .badge-* in styles.css). */
    public static void styleBadge(Label badge, String status) {
        badge.setText(label(status).toUpperCase());
        badge.getStyleClass().removeAll("badge", "badge-draft", "badge-open", "badge-progress", "badge-completed");
        badge.getStyleClass().add("badge");
        badge.getStyleClass().add(switch (status == null ? "" : status) {
            case OPEN -> "badge-open";
            case IN_PROGRESS -> "badge-progress";
            case COMPLETED -> "badge-completed";
            default -> "badge-draft";
        });
    }
}
