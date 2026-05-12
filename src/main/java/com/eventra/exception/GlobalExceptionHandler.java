package com.eventra.exception;

import com.eventra.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RESOURCE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmail(
            DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("DUPLICATE_EMAIL", ex.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(
            InvalidTokenException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_TOKEN", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("UNAUTHORIZED", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(VendorProfileAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleVendorExists(
            VendorProfileAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("VENDOR_PROFILE_EXISTS", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Eroare neașteptată: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR",
                        "A apărut o eroare internă."));
    }
}