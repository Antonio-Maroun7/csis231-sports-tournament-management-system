package com.antonio.sportstournamentclient.team.dto;
import java.time.LocalDateTime;
/** Team returned by the API. */ public record TeamResponse(Long id,String teamName,Long managerId,String managerName,LocalDateTime createdAt) {}

