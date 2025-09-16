package com.paklog.cartonization.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(
    description = "Standard error response format",
    example = """
        {
          "error": "Validation Failed",
          "message": "Request validation failed",
          "details": [
            "Item SKU is required",
            "Item quantity must be positive"
          ],
          "timestamp": "2024-01-15T10:30:00Z",
          "path": "/api/v1/packing-solutions"
        }
        """
)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    @Schema(
        description = "Error type or category",
        example = "Validation Failed"
    )
    String error,
    
    @Schema(
        description = "Human-readable error message",
        example = "Request validation failed"
    )
    String message,
    
    @Schema(
        description = "Additional error details or validation errors",
        example = "[\"Item SKU is required\", \"Item quantity must be positive\"]"
    )
    List<String> details,
    
    @Schema(
        description = "Timestamp when the error occurred",
        example = "2024-01-15T10:30:00Z"
    )
    Instant timestamp,
    
    @Schema(
        description = "API path where the error occurred",
        example = "/api/v1/packing-solutions"
    )
    String path,
    
    @Schema(
        description = "Request ID for tracing",
        example = "req-12345"
    )
    String requestId
) {
    
    public static ErrorResponse of(String error, String message, String path) {
        return new ErrorResponse(error, message, null, Instant.now(), path, null);
    }
    
    public static ErrorResponse of(String error, String message, List<String> details, String path) {
        return new ErrorResponse(error, message, details, Instant.now(), path, null);
    }
    
    public static ErrorResponse of(String error, String message, String path, String requestId) {
        return new ErrorResponse(error, message, null, Instant.now(), path, requestId);
    }
    
    public static ErrorResponse of(String error, String message, List<String> details, String path, String requestId) {
        return new ErrorResponse(error, message, details, Instant.now(), path, requestId);
    }
}