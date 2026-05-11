package com.eventra.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Token-ul este obligatoriu.")
        String token,

        @NotBlank(message = "Parola nouă este obligatorie.")
        @Size(min = 8, max = 100, message = "Parola trebuie să aibă cel puțin 8 caractere.")
        String newPassword
) {}