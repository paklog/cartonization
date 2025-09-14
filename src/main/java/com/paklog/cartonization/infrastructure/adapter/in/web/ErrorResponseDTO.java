package com.paklog.cartonization.infrastructure.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {
    String code;
    String message;
    Map<String, String> details;
    Instant timestamp;
    String path;
}