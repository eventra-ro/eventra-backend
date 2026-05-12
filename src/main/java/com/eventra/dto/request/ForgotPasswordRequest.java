package com.eventra.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank
        @Email(message = "Adresa de email nu este validă.")
        String email
) {}