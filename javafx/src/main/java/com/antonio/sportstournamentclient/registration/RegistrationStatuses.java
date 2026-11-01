package com.antonio.sportstournamentclient.registration;

import javafx.scene.control.Label;
import javafx.scene.control.TableCell;

/**
 * Registration statuses PENDING / APPROVED / REJECTED: labels and coloured badges.
 */
public class RegistrationStatuses {
    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";

    /** Turns a Label into a coloured status badge (see .badge-* in styles.css). */
    public static void styleBadge(Label badge, String status) {
        badge.setText(status == null ? "" : status);
        badge.getStyleClass().removeAll("badge", "badge-pending", "badge-approved", "badge-rejected");
        badge.getStyleClass().add("badge");
        badge.getStyleClass().add(switch (status == null ? "" : status) {
            case APPROVED -> "badge-approved";
            case REJECTED -> "badge-rejected";
            default -> "badge-pending";
        });
    }

    /** Table cell that shows a registration status as a badge. */
    public static class BadgeCell<S> extends TableCell<S, String> {
        private final Label badge = new Label();

        @Override
        protected void updateItem(String status, boolean empty) {
            super.updateItem(status, empty);
            setText(null);
            if (empty || status == null) {
                setGraphic(null);
            } else {
                styleBadge(badge, status);
                setGraphic(badge);
            }
        }
    }
}
