package org.example.fincore.auth.dto;

public record RefreshResponseDto(
        String accessToken,
        String refreshToken
) {
}
