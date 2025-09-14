package com.paklog.cartonization.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PerformanceAspect {

    private final PackingMetricsService metricsService;

    @Around("@annotation(com.paklog.cartonization.infrastructure.config.Loggable)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant start = Instant.now();

        try {
            Object result = joinPoint.proceed();

            Instant end = Instant.now();
            long duration = Duration.between(start, end).toMillis();

            log.info("{} executed in {} ms", joinPoint.getSignature(), duration);

            // Record metrics for packing algorithm
            if (joinPoint.getSignature().getName().contains("calculate")) {
                metricsService.recordAlgorithmExecution(duration);
            }

            return result;

        } catch (Exception e) {
            Instant end = Instant.now();
            long duration = Duration.between(start, end).toMillis();

            log.error("{} failed after {} ms: {}", joinPoint.getSignature(), duration, e.getMessage());
            throw e;
        }
    }

    @Around("@annotation(com.paklog.cartonization.infrastructure.config.Cacheable)")
    public Object cacheResult(ProceedingJoinPoint joinPoint) throws Throwable {
        // Custom caching logic can be implemented here
        // For now, delegate to Spring's caching mechanism
        return joinPoint.proceed();
    }
}