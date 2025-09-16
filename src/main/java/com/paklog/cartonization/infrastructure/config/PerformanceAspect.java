package com.paklog.cartonization.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Aspect
@Component
public class PerformanceAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceAspect.class);
    
    private final PackingMetricsService metricsService;
    
    public PerformanceAspect(PackingMetricsService metricsService) {
        this.metricsService = metricsService;
    }

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

}