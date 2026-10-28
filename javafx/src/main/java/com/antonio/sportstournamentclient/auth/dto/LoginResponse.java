package com.antonio.sportstournamentclient.auth.dto;
/** Authenticated user returned by the API. */ public record LoginResponse(Long id,String name,String email,String role,boolean twoFactorEnabled) {}

