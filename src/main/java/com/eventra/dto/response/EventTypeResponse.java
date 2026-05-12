package com.eventra.dto.response;

import java.util.UUID;

public record EventTypeResponse(
        UUID id,
        String code,
        String name,
        String icon,
        int sortOrder
) {}