package com.paklog.cartonization.infrastructure.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {
    private final String code;
    private final String message;
    private final Map<String, String> details;
    private final Instant timestamp;
    private final String path;
    
    public ErrorResponseDTO(String code, String message, Map<String, String> details, 
                           Instant timestamp, String path) {
        this.code = code;
        this.message = message;
        this.details = details;
        this.timestamp = timestamp;
        this.path = path;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Map<String, String> getDetails() {
        return details;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public String getPath() {
        return path;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorResponseDTO that = (ErrorResponseDTO) o;
        return Objects.equals(code, that.code) &&
               Objects.equals(message, that.message) &&
               Objects.equals(details, that.details) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(path, that.path);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(code, message, details, timestamp, path);
    }
    
    @Override
    public String toString() {
        return "ErrorResponseDTO{" +
               "code='" + code + '\'' +
               ", message='" + message + '\'' +
               ", details=" + details +
               ", timestamp=" + timestamp +
               ", path='" + path + '\'' +
               '}';
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String code;
        private String message;
        private Map<String, String> details;
        private Instant timestamp;
        private String path;
        
        public Builder code(String code) {
            this.code = code;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder details(Map<String, String> details) {
            this.details = details;
            return this;
        }
        
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder path(String path) {
            this.path = path;
            return this;
        }
        
        public ErrorResponseDTO build() {
            return new ErrorResponseDTO(code, message, details, timestamp, path);
        }
    }
}