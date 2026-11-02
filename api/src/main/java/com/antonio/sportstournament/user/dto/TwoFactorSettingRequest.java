package com.antonio.sportstournament.user.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request body of {@code PUT /api/users/{id}/two-factor}: turns email two-factor authentication on or off.
 *
 * @param enabled required; true to enable, false to disable (not allowed for ADMIN and ORGANIZER)
 */
public record TwoFactorSettingRequest(@NotNull Boolean enabled) {
}
