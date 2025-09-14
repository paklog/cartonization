package com.paklog.cartonization.infrastructure.adapter.in.web;

import com.paklog.cartonization.domain.exception.CartonNotFoundException;
import com.paklog.cartonization.domain.exception.InvalidPackingRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CartonNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleCartonNotFound(
            CartonNotFoundException ex,
            WebRequest request) {

        log.error("Carton not found: {}", ex.getMessage());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .code("CARTON_NOT_FOUND")
            .message(ex.getMessage())
            .timestamp(Instant.now())
            .path(request.getDescription(false))
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidPackingRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidPackingRequest(
            InvalidPackingRequestException ex,
            WebRequest request) {

        log.error("Invalid packing request: {}", ex.getMessage());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .code("INVALID_PACKING_REQUEST")
            .message(ex.getMessage())
            .timestamp(Instant.now())
            .path(request.getDescription(false))
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .code("VALIDATION_ERROR")
            .message("Request validation failed")
            .details(errors)
            .timestamp(Instant.now())
            .path(request.getDescription(false))
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex,
            WebRequest request) {

        log.error("Unexpected error occurred", ex);

        ErrorResponseDTO error = ErrorResponseDTO.builder()
            .code("INTERNAL_ERROR")
            .message("An unexpected error occurred")
            .timestamp(Instant.now())
            .path(request.getDescription(false))
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}