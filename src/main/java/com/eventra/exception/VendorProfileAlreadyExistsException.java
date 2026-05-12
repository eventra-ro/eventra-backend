package com.eventra.exception;

public class VendorProfileAlreadyExistsException extends RuntimeException {
    public VendorProfileAlreadyExistsException() {
        super("Ai deja un profil de furnizor creat.");
    }
}