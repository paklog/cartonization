package com.paklog.cartonization.infrastructure.adapter.in.web;

import com.paklog.cartonization.infrastructure.cache.CacheWarmupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@Tag(name = "Health & Status", description = "API for health checks and system status")
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    private final CacheWarmupService cacheWarmupService;
    private final Instant startupTime;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${app.version:1.0.0}")
    private String applicationVersion;

    public HealthController(CacheWarmupService cacheWarmupService) {
        this.cacheWarmupService = cacheWarmupService;
        this.startupTime = Instant.now();
    }

    @Operation(
        summary = "Health check endpoint",
        description = "Returns the health status of the cartonization service and its dependencies."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Service is healthy",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = HealthResponse.class),
                examples = @ExampleObject(
                    name = "Healthy service",
                    value = """
                        {
                          "status": "UP",
                          "application": "Cartonization Service",
                          "version": "1.0.0",
                          "timestamp": "2024-01-15T10:30:00Z",
                          "uptime": "PT2H30M",
                          "cacheStatus": {
                            "enabled": true,
                            "cartonsCached": true,
                            "productsCached": true,
                            "availableCaches": ["cartons", "products", "packing-solutions"]
                          }
                        }
                        """
                )
            )
        )
    })
    @GetMapping
    public ResponseEntity<HealthResponse> health() {
        log.debug("Health check requested");
        
        CacheWarmupService.CacheWarmupStatus cacheStatus = cacheWarmupService.getWarmupStatus();
        
        HealthResponse response = new HealthResponse(
            "UP",
            applicationName,
            applicationVersion,
            Instant.now(),
            java.time.Duration.between(startupTime, Instant.now()).toString(),
            cacheStatus
        );
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Detailed system information",
        description = "Returns detailed information about the cartonization service including runtime metrics and configuration."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "System information retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SystemInfoResponse.class)
            )
        )
    })
    @GetMapping("/info")
    public ResponseEntity<SystemInfoResponse> systemInfo() {
        log.debug("System info requested");
        
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        Map<String, Object> jvmInfo = Map.of(
            "availableProcessors", runtime.availableProcessors(),
            "totalMemory", totalMemory,
            "freeMemory", freeMemory, 
            "usedMemory", usedMemory,
            "maxMemory", maxMemory,
            "memoryUsagePercentage", Math.round((double) usedMemory / maxMemory * 100.0)
        );
        
        Map<String, Object> systemProps = Map.of(
            "javaVersion", System.getProperty("java.version"),
            "javaVendor", System.getProperty("java.vendor"),
            "osName", System.getProperty("os.name"),
            "osVersion", System.getProperty("os.version"),
            "osArch", System.getProperty("os.arch")
        );
        
        SystemInfoResponse response = new SystemInfoResponse(
            applicationName,
            applicationVersion,
            Instant.now(),
            java.time.Duration.between(startupTime, Instant.now()).toString(),
            jvmInfo,
            systemProps,
            cacheWarmupService.getWarmupStatus()
        );
        
        return ResponseEntity.ok(response);
    }

    @Schema(description = "Health check response")
    public record HealthResponse(
        @Schema(description = "Overall health status", example = "UP")
        String status,
        
        @Schema(description = "Application name", example = "Cartonization Service")
        String application,
        
        @Schema(description = "Application version", example = "1.0.0")
        String version,
        
        @Schema(description = "Current timestamp", example = "2024-01-15T10:30:00Z")
        Instant timestamp,
        
        @Schema(description = "Service uptime", example = "PT2H30M")
        String uptime,
        
        @Schema(description = "Cache warmup status")
        CacheWarmupService.CacheWarmupStatus cacheStatus
    ) {}

    @Schema(description = "Detailed system information response")
    public record SystemInfoResponse(
        @Schema(description = "Application name")
        String application,
        
        @Schema(description = "Application version")
        String version,
        
        @Schema(description = "Current timestamp")
        Instant timestamp,
        
        @Schema(description = "Service uptime")
        String uptime,
        
        @Schema(description = "JVM runtime information")
        Map<String, Object> jvm,
        
        @Schema(description = "System properties")
        Map<String, Object> system,
        
        @Schema(description = "Cache status")
        CacheWarmupService.CacheWarmupStatus cacheStatus
    ) {}
}