package com.antonio.sportstournamentclient.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Presentation formatting used by views. */
public final class Formats {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private Formats() {
    }

    public static String dateTime(LocalDateTime value) {
        return value == null ? "" : DATE_TIME.format(value);
    }
}
