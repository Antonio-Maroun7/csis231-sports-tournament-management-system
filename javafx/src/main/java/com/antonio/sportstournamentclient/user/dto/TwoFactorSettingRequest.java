package com.antonio.sportstournamentclient.user.dto;

/**
 * JSON body of PUT /api/users/{id}/two-factor: {"enabled": true}.
 */
public record TwoFactorSettingRequest(boolean enabled) {
}
