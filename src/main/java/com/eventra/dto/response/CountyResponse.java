package com.eventra.dto.response;

import java.util.UUID;

public record CountyResponse(
        UUID id,
        String code,
        String name
) {}