package com.eventra.dto.request;

import com.eventra.entity.enums.UserRole;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank(message = "Numele complet este obligatoriu.")
        @Size(min = 2, max = 255)
        String fullName,

        @NotBlank(message = "Emailul este obligatoriu.")
        @Email(message = "Adresa de email nu este validă.")
        @Size(max = 255)
        String email,

        @NotBlank(message = "Parola este obligatorie.")
        @Size(min = 8, max = 100, message = "Parola trebuie să aibă cel puțin 8 caractere.")
        String password,

        @NotNull(message = "Tipul contului este obligatoriu.")
        UserRole role
) {}