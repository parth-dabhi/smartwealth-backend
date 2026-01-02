package com.smartwealth.smartwealth_backend.exception;

import com.smartwealth.smartwealth_backend.dto.response.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());

        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());

        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Resource already exists: {}", ex.getMessage());

        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        log.warn("Validation error: {}", message);

        return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseException(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Database constraint violation", ex);

        return buildErrorResponse(HttpStatus.CONFLICT, "Data integrity violation: " + ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);

        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.: " + ex.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("No resource found: {}", ex.getMessage());

        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON request: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request: " + ex.getMessage(), request);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<?> handleAuthorizationDenied(AuthorizationDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("status", 403, "error", "FORBIDDEN", "message", "Access denied", "path", request.getRequestURI()));
    }

    @ExceptionHandler(KycTransitionException.class)
    public ResponseEntity<ErrorResponse> handleKycTransitionException(KycTransitionException ex, HttpServletRequest request) {
        log.warn("Invalid KYC status transition: {}", ex.getMessage());

        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity
                .status(status)
                .body(ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(message)
                        .path(request.getRequestURI())
                        .build()
                );
    }
}
