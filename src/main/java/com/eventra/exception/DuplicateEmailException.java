package com.eventra.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super("Adresa de email este deja înregistrată.");
    }
}