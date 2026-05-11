package com.eventra.dto.response;

import com.eventra.entity.enums.UserRole;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserRole role,
        UUID userId,
        String fullName
) {}