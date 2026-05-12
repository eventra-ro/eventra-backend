package com.eventra.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record CreateVendorProfileRequest(
        @NotBlank(message = "Numele business-ului este obligatoriu.")
        @Size(max = 255)
        String businessName,

        @NotBlank(message = "Telefonul este obligatoriu.")
        @Size(max = 50)
        String phone,

        @Size(max = 1000)
        String description,

        @Size(max = 500)
        String websiteUrl,

        @Size(max = 500)
        String instagramUrl,

        @Size(max = 500)
        String facebookUrl,

        @DecimalMin(value = "0.0")
        BigDecimal priceFrom,

        @DecimalMin(value = "0.0")
        BigDecimal priceTo,

        @Size(min = 3, max = 3)
        String priceCurrency,

        @NotEmpty(message = "Cel puțin o categorie este obligatorie.")
        @Size(max = 3, message = "Maximum 3 categorii.")
        List<String> categoryCodes,

        @NotEmpty(message = "Cel puțin un județ este obligatoriu.")
        List<String> countyCodes,

        List<String> eventTypeCodes
) {}