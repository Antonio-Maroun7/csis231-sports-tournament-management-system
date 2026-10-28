package com.antonio.sportstournamentclient.user.dto;
import java.time.LocalDateTime;
/** User returned by the API. */ public record UserResponse(Long id,String name,String email,String role,boolean twoFactorEnabled,LocalDateTime createdAt) {}

