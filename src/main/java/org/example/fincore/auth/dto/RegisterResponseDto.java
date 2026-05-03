package org.example.fincore.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.fincore.user.entity.UserStatus;

public record RegisterResponseDto(
        @Email @NotBlank String email,
        UserStatus userStatus
) {
}
