package com.paklog.cartonization.domain.exception;

public class CartonNotFoundException extends RuntimeException {
    public CartonNotFoundException(String message) {
        super(message);
    }

    public CartonNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}