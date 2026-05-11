package com.eventra.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "Token-ul de verificare este obligatoriu.")
        String token
) {}