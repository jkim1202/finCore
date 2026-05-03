package org.example.fincore.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RegisterRequestDto(
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotBlank String phone,
        @NotBlank String name,
        @NotNull LocalDate birthDate
        ) {
}
