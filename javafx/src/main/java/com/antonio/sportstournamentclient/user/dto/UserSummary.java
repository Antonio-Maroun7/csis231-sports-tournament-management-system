package com.antonio.sportstournamentclient.user.dto;
/** Compact user representation used in selectors and tables. */
public record UserSummary(Long id, String name, String email, String role) {}

