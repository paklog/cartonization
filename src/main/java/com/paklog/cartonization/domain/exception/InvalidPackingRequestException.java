package com.paklog.cartonization.domain.exception;

public class InvalidPackingRequestException extends RuntimeException {
    public InvalidPackingRequestException(String message) {
        super(message);
    }

    public InvalidPackingRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}