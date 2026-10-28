package com.antonio.sportstournamentclient.user.dto;
/** Administrator user creation payload. */
public record UserCreateRequest(String name, String email, String password, String role) {}

