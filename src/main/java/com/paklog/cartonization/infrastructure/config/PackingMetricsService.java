package com.paklog.cartonization.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class PackingMetricsService {

    private final MeterRegistry meterRegistry;

    // Counters for business metrics - initialized in constructor
    private final Counter packingRequestsCounter;
    private final Counter packingSuccessCounter;
    private final Counter packingFailureCounter;
    private final Timer packingAlgorithmTimer;

    public PackingMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.packingRequestsCounter = Counter.builder("cartonization.packing.requests")
            .description("Total number of packing solution requests")
            .register(meterRegistry);
        this.packingSuccessCounter = Counter.builder("cartonization.packing.success")
            .description("Number of successful packing solutions")
            .register(meterRegistry);
        this.packingFailureCounter = Counter.builder("cartonization.packing.failure")
            .description("Number of failed packing solutions")
            .register(meterRegistry);
        this.packingAlgorithmTimer = Timer.builder("cartonization.algorithm.duration")
            .description("Time taken to execute packing algorithm")
            .register(meterRegistry);
    }


    // Gauges for current state
    private volatile double averageUtilization = 0.0;
    private volatile int totalPackages = 0;

    public void recordPackingRequest() {
        packingRequestsCounter.increment();
    }

    public void recordPackingSuccess() {
        packingSuccessCounter.increment();
    }

    public void recordPackingFailure() {
        packingFailureCounter.increment();
    }

    public void recordAlgorithmExecution(long durationMillis) {
        packingAlgorithmTimer.record(Duration.ofMillis(durationMillis));
    }

    public void updateAverageUtilization(double utilization) {
        this.averageUtilization = utilization;
        meterRegistry.gauge("cartonization.utilization.average", this, PackingMetricsService::getAverageUtilization);
    }

    public void updateTotalPackages(int packages) {
        this.totalPackages = packages;
        meterRegistry.gauge("cartonization.packages.total", this, PackingMetricsService::getTotalPackages);
    }

    public double getAverageUtilization() {
        return averageUtilization;
    }

    public int getTotalPackages() {
        return totalPackages;
    }

    public void recordCartonCreated() {
        Counter.builder("cartonization.cartons.created")
            .description("Number of cartons created")
            .register(meterRegistry)
            .increment();
    }

    public void recordCartonDeactivated() {
        Counter.builder("cartonization.cartons.deactivated")
            .description("Number of cartons deactivated")
            .register(meterRegistry)
            .increment();
    }
}