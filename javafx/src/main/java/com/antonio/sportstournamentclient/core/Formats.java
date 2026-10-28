package com.antonio.sportstournamentclient.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Display formats shared by the views.
 */
public class Formats {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);

    /** 2026-10-18T10:25 -> "Oct 18, 2026" */
    public static String date(LocalDateTime dateTime) {
        return dateTime == null ? "" : DATE.format(dateTime);
    }

    /** TEAM_MANAGER -> "Team Manager" */
    public static String role(String role) {
        if (role == null) {
            return "";
        }
        return switch (role) {
            case "ADMIN" -> "Administrator";
            case "ORGANIZER" -> "Organizer";
            case "TEAM_MANAGER" -> "Team Manager";
            default -> role;
        };
    }

    /** "Antonio Maroun" -> "AM" (avatar in the sidebar) */
    public static String initials(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase();
    }
}
