package com.antonio.sportstournament.dto;

import com.antonio.sportstournament.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(

        @NotBlank @Size(max = 100)
        String name,

        @NotBlank @Email  @Size(max = 255)
        String email,

        @NotBlank @Size(min = 6,max = 100)
        String password,

        @NotNull
        Role role
) {
}
