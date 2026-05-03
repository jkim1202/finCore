package org.example.fincore.auth.dto;

public record LoginResponseDto(
        String accessToken,
        String refreshToken
) {
}
