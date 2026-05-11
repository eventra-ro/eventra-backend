package com.eventra.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Emailul este obligatoriu.")
        @Email(message = "Adresa de email nu este validă.")
        String email,

        @NotBlank(message = "Parola este obligatorie.")
        String password
) {}