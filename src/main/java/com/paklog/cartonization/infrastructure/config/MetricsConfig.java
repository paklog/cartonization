package com.paklog.cartonization.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.config.MeterFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    private static final Logger log = LoggerFactory.getLogger(MetricsConfig.class);

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${app.version:1.0.0}")
    private String applicationVersion;

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            registry.config()
                .commonTags("application", applicationName)
                .commonTags("version", applicationVersion)
                .meterFilter(MeterFilter.deny(id -> {
                    // Filter out noisy metrics
                    String name = id.getName();
                    return name.startsWith("jvm.gc.pause") && name.contains("unknown");
                }));

            log.info("Metrics configured with common tags - application: {}, version: {}", 
                    applicationName, applicationVersion);
        };
    }

    @Bean
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    @Bean
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    @Bean
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    @Bean
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    @Bean
    public UptimeMetrics uptimeMetrics() {
        return new UptimeMetrics();
    }

    // Custom business metrics
    @Bean
    public Counter packingSolutionCalculatedCounter(MeterRegistry registry) {
        return Counter.builder("cartonization.packing.solution.calculated")
            .description("Number of packing solutions calculated")
            .register(registry);
    }

    @Bean
    public Timer packingSolutionCalculationTimer(MeterRegistry registry) {
        return Timer.builder("cartonization.packing.solution.calculation.time")
            .description("Time taken to calculate packing solutions")
            .register(registry);
    }

    @Bean
    public Counter packingSolutionErrorCounter(MeterRegistry registry) {
        return Counter.builder("cartonization.packing.solution.errors")
            .description("Number of packing solution calculation errors")
            .register(registry);
    }

    @Bean
    public Counter cloudEventPublishedCounter(MeterRegistry registry) {
        return Counter.builder("cartonization.events.published")
            .description("Number of CloudEvents published")
            .tag("event_type", "unknown")
            .register(registry);
    }

    @Bean
    public Counter cloudEventConsumedCounter(MeterRegistry registry) {
        return Counter.builder("cartonization.events.consumed")
            .description("Number of CloudEvents consumed")
            .tag("event_type", "unknown")
            .register(registry);
    }

    @Bean
    public Timer cloudEventProcessingTimer(MeterRegistry registry) {
        return Timer.builder("cartonization.events.processing.time")
            .description("Time taken to process CloudEvents")
            .register(registry);
    }

    @Bean
    public Counter cacheHitCounter(MeterRegistry registry) {
        return Counter.builder("cartonization.cache.hits")
            .description("Number of cache hits")
            .tag("cache_name", "unknown")
            .register(registry);
    }

    @Bean
    public Counter cacheMissCounter(MeterRegistry registry) {
        return Counter.builder("cartonization.cache.misses")
            .description("Number of cache misses")
            .tag("cache_name", "unknown")
            .register(registry);
    }

    @Bean
    public Counter productCatalogCallCounter(MeterRegistry registry) {
        return Counter.builder("cartonization.product_catalog.calls")
            .description("Number of product catalog API calls")
            .tag("operation", "unknown")
            .register(registry);
    }

    @Bean
    public Timer productCatalogCallTimer(MeterRegistry registry) {
        return Timer.builder("cartonization.product_catalog.call.time")
            .description("Time taken for product catalog API calls")
            .register(registry);
    }

    @Bean
    public Counter circuitBreakerStateCounter(MeterRegistry registry) {
        return Counter.builder("cartonization.circuit_breaker.state_changes")
            .description("Circuit breaker state changes")
            .tag("circuit_breaker", "unknown")
            .tag("state", "unknown")
            .register(registry);
    }

    @Bean
    public Counter databaseOperationCounter(MeterRegistry registry) {
        return Counter.builder("cartonization.database.operations")
            .description("Number of database operations")
            .tag("operation", "unknown")
            .tag("entity", "unknown")
            .register(registry);
    }

    @Bean
    public Timer databaseOperationTimer(MeterRegistry registry) {
        return Timer.builder("cartonization.database.operation.time")
            .description("Time taken for database operations")
            .tag("operation", "unknown")
            .tag("entity", "unknown")
            .register(registry);
    }
}