package com.eventra.dto.response;

import java.util.UUID;

public record VendorCategoryResponse(
        UUID id,
        String code,
        String name,
        String description,
        int sortOrder
) {}