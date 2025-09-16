package com.paklog.cartonization.infrastructure.adapter.in.web;

import com.paklog.cartonization.domain.exception.CartonNotFoundException;
import com.paklog.cartonization.domain.exception.InvalidPackingRequestException;
import com.paklog.cartonization.infrastructure.adapter.in.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CartonNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCartonNotFound(
            CartonNotFoundException ex, HttpServletRequest request) {
        
        log.warn("Carton not found for request: {}", request.getRequestURI(), ex);
        
        String requestId = getRequestId(request);
        ErrorResponse errorResponse = ErrorResponse.of(
            "Not Found",
            ex.getMessage(),
            request.getRequestURI(),
            requestId
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InvalidPackingRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPackingRequest(
            InvalidPackingRequestException ex, HttpServletRequest request) {
        
        log.warn("Invalid packing request for request: {}", request.getRequestURI(), ex);
        
        String requestId = getRequestId(request);
        ErrorResponse errorResponse = ErrorResponse.of(
            "Business Rule Violation",
            ex.getMessage(),
            request.getRequestURI(),
            requestId
        );
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        log.warn("Validation failed for request: {}", request.getRequestURI(), ex);
        
        List<String> details = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.add(error.getField() + ": " + error.getDefaultMessage());
        }
        
        String requestId = getRequestId(request);
        ErrorResponse errorResponse = ErrorResponse.of(
            "Validation Failed",
            "Request validation failed",
            details,
            request.getRequestURI(),
            requestId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        log.warn("Constraint violation for request: {}", request.getRequestURI(), ex);
        
        List<String> details = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            details.add(violation.getPropertyPath() + ": " + violation.getMessage());
        }
        
        String requestId = getRequestId(request);
        ErrorResponse errorResponse = ErrorResponse.of(
            "Constraint Violation",
            "Request constraint validation failed",
            details,
            request.getRequestURI(),
            requestId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        log.warn("Type mismatch for request: {}", request.getRequestURI(), ex);
        
        String message = String.format(
            "Invalid value '%s' for parameter '%s'. Expected type: %s",
            ex.getValue(),
            ex.getName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );
        
        String requestId = getRequestId(request);
        ErrorResponse errorResponse = ErrorResponse.of(
            "Type Mismatch",
            message,
            request.getRequestURI(),
            requestId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        log.warn("Illegal argument for request: {}", request.getRequestURI(), ex);
        
        String requestId = getRequestId(request);
        ErrorResponse errorResponse = ErrorResponse.of(
            "Invalid Request",
            ex.getMessage(),
            request.getRequestURI(),
            requestId
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleBusinessLogicException(
            RuntimeException ex, HttpServletRequest request) {
        
        log.warn("Business logic error for request: {}", request.getRequestURI(), ex);
        
        String requestId = getRequestId(request);
        ErrorResponse errorResponse;
        
        // Check if it's a business rule violation
        if (ex.getMessage() != null && ex.getMessage().contains("business rule")) {
            errorResponse = ErrorResponse.of(
                "Business Rule Violation",
                ex.getMessage(),
                request.getRequestURI(),
                requestId
            );
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
        }
        
        // Check if it's a not found error
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("not found")) {
            errorResponse = ErrorResponse.of(
                "Not Found",
                ex.getMessage(),
                request.getRequestURI(),
                requestId
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        // Generic business logic error
        errorResponse = ErrorResponse.of(
            "Processing Error",
            ex.getMessage(),
            request.getRequestURI(),
            requestId
        );
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        log.error("Unexpected error for request: {}", request.getRequestURI(), ex);
        
        String requestId = getRequestId(request);
        ErrorResponse errorResponse = ErrorResponse.of(
            "Internal Server Error",
            "An unexpected error occurred. Please contact support if the problem persists.",
            request.getRequestURI(),
            requestId
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private String getRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null) {
            requestId = (String) request.getAttribute("requestId");
        }
        return requestId;
    }
}