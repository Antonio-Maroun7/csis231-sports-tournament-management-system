package com.antonio.sportstournament.user.dto;

import jakarta.validation.constraints.NotNull;

/** Request to enable or disable email-based two-factor authentication. */
public record TwoFactorSettingRequest(@NotNull Boolean enabled) {
}
