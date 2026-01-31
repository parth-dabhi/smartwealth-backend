package com.smartwealth.smartwealth_backend.exception;

import com.smartwealth.smartwealth_backend.dto.response.common.ErrorResponse;
import com.smartwealth.smartwealth_backend.exception.auth.AuthenticationException;
import com.smartwealth.smartwealth_backend.exception.mutual_fund.*;
import com.smartwealth.smartwealth_backend.exception.nav.NavHistoryNotFoundException;
import com.smartwealth.smartwealth_backend.exception.resource.ResourceAlreadyExistsException;
import com.smartwealth.smartwealth_backend.exception.resource.ResourceNotFoundException;
import com.smartwealth.smartwealth_backend.exception.transaction.IdempotencyKeyExpiredException;
import com.smartwealth.smartwealth_backend.exception.transaction.InvalidTransactionStateException;
import com.smartwealth.smartwealth_backend.exception.transaction.TransactionFailedException;
import com.smartwealth.smartwealth_backend.exception.user.InactiveAccountException;
import com.smartwealth.smartwealth_backend.exception.user.KycTransitionException;
import com.smartwealth.smartwealth_backend.exception.user.KycVerificationException;
import com.smartwealth.smartwealth_backend.exception.wallet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException ex, HttpServletRequest request) {
        log.warn("Missing request header: {}", ex.getMessage());

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Missing request header: " + ex.getHeaderName(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());

        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("Missing request parameter: {}", ex.getMessage());

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Missing request parameter: " + ex.getParameterName(), request);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalanceException(InsufficientBalanceException ex, HttpServletRequest request) {
        log.warn("Insufficient balance: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotFoundException(WalletNotFoundException ex, HttpServletRequest request) {
        log.warn("Wallet not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InactiveAccountException.class)
    public ResponseEntity<ErrorResponse> handleInactiveAccountException(InactiveAccountException ex, HttpServletRequest request) {
        log.warn("Inactive account: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(KycVerificationException.class)
    public ResponseEntity<ErrorResponse> handleKycVerificationException(KycVerificationException ex, HttpServletRequest request) {
        log.warn("KYC verification issue: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(WalletLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleWalletLimitExceededException(WalletLimitExceededException ex, HttpServletRequest request) {
        log.warn("Wallet limit exceeded: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(IdempotencyKeyExpiredException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyKeyExpiredException(IdempotencyKeyExpiredException ex, HttpServletRequest request) {
        log.warn("Idempotency key expired: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.GONE, ex.getMessage(), request);
    }

    @ExceptionHandler(TransactionFailedException.class)
    public ResponseEntity<ErrorResponse> handleTransactionFailedException(TransactionFailedException ex, HttpServletRequest request) {
        log.warn("Transaction failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(WalletSuspendedException.class)
    public ResponseEntity<ErrorResponse> handleWalletSuspendedException(WalletSuspendedException ex, HttpServletRequest request) {
        log.warn("Wallet is suspended: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(PlanNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlanNotFoundException(PlanNotFoundException ex, HttpServletRequest request) {
        log.warn("Plan not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(NavHistoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNavDataNotFoundException(NavHistoryNotFoundException ex, HttpServletRequest request) {
        log.warn("NAV data not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(SipExecutionFailedException.class)
    public ResponseEntity<ErrorResponse> handleSipExecutionFailedException(SipExecutionFailedException ex, HttpServletRequest request) {
        log.warn("SIP execution failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(HoldingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleHoldingNotFoundException(HoldingNotFoundException ex, HttpServletRequest request) {
        log.warn("Holding not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(WalletTransactionException.class)
    public ResponseEntity<ErrorResponse> handleWalletTransactionException(WalletTransactionException ex, HttpServletRequest request) {
        log.warn("Wallet transaction error (type: {}): {}", ex.getFailureType(), ex.getMessage());

        HttpStatus status = switch (ex.getFailureType()) {
            case INSUFFICIENT_BALANCE, IDEMPOTENCY_KEY_EXPIRED, LIMIT_EXCEEDED -> HttpStatus.BAD_REQUEST;
            case WALLET_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case WALLET_SUSPENDED -> HttpStatus.FORBIDDEN;
            case TRANSACTION_FAILED -> HttpStatus.UNPROCESSABLE_ENTITY;
            case DATABASE_ERROR, UNKNOWN_ERROR -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return buildErrorResponse(status, ex.getMessage(), request);
    }

    @ExceptionHandler(SipNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleSipNotAllowedException(SipNotAllowedException ex, HttpServletRequest request) {
        log.warn("SIP not allowed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidSipConfigurationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSipConfigurationException(InvalidSipConfigurationException ex, HttpServletRequest request) {
        log.warn("Invalid SIP configuration: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidSellConfigurationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSellConfigurationException(InvalidSellConfigurationException ex, HttpServletRequest request) {
        log.warn("Invalid SELL configuration: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidBuyConfigurationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBuyConfigurationException(InvalidBuyConfigurationException ex, HttpServletRequest request) {
        log.warn("Invalid BUY configuration: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(HoldingUpdateFailedException.class)
    public ResponseEntity<ErrorResponse> handleHoldingUpdateFailedException(HoldingUpdateFailedException ex, HttpServletRequest request) {
        log.warn("Holding update failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidSellRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSellRequestException(InvalidSellRequestException ex, HttpServletRequest request) {
        log.warn("Invalid SELL request: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(SipMandateNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSipMandateNotFoundException(SipMandateNotFoundException ex, HttpServletRequest request) {
        log.warn("SIP Mandate not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidSipStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSipStateException(InvalidSipStateException ex, HttpServletRequest request) {
        log.warn("Invalid SIP state: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidTransactionStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransactionStateException(InvalidTransactionStateException ex, HttpServletRequest request) {
        log.warn("Invalid transaction state: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidInvestmentOrderException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInvestmentOrderException(InvalidInvestmentOrderException ex, HttpServletRequest request) {
        log.warn("Invalid investment order: {}", ex.getMessage());
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
