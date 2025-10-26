package com.paklog.cartonization.domain.model.entity;

import com.paklog.cartonization.domain.model.valueobject.ItemWithDimensions;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PackingSolution {
    private final String solutionId;
    private String requestId;
    private String orderId;
    private final List<Package> packages;
    private final Instant createdAt;

    private PackingSolution(String solutionId, List<Package> packages) {
        this.solutionId = solutionId;
        this.packages = packages;
        this.createdAt = Instant.now();
    }


    public static PackingSolution create(List<Package> packages) {
        String solutionId = "sol-" + UUID.randomUUID().toString().substring(0, 12);
        return new PackingSolution(solutionId, packages);
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getTotalPackages() {
        return packages.size();
    }

    public int getTotalItems() {
        return packages.stream()
            .mapToInt(Package::getItemCount)
            .sum();
    }

    public BigDecimal getTotalWeight() {
        return packages.stream()
            .map(pkg -> pkg.getCarton().getMaxWeight().getValue())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getAverageUtilization() {
        if (packages.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalUtilization = packages.stream()
            .map(Package::getUtilization)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalUtilization.divide(
            BigDecimal.valueOf(packages.size()),
            4,
            RoundingMode.HALF_UP
        );
    }

    public BigDecimal getTotalVolume() {
        return packages.stream()
            .map(pkg -> pkg.getCarton().getDimensions().volume())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getUsedVolume() {
        return packages.stream()
            .map(Package::getUsedVolume)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<ItemWithDimensions> getAllItems() {
        return packages.stream()
            .flatMap(pkg -> pkg.getItems().stream())
            .toList();
    }

    public boolean isEmpty() {
        return packages.isEmpty() || packages.stream().allMatch(Package::isEmpty);
    }

    public String getSolutionId() {
        return solutionId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getOrderId() {
        return orderId;
    }

    public List<Package> getPackages() {
        return packages;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}