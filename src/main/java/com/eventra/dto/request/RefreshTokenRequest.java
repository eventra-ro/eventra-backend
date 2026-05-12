package com.eventra.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token-ul este obligatoriu.")
        String refreshToken
) {}