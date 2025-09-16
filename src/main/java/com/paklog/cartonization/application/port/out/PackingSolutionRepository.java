package com.paklog.cartonization.application.port.out;

import com.paklog.cartonization.domain.model.entity.PackingSolution;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PackingSolutionRepository {

    PackingSolution save(PackingSolution solution);

    Optional<PackingSolution> findById(String solutionId);

    Optional<PackingSolution> findByRequestId(String requestId);

    List<PackingSolution> findByOrderId(String orderId);

    List<PackingSolution> findRecentSolutions(int limit);

    List<PackingSolution> findSolutionsCreatedAfter(Instant since);

    List<PackingSolution> findSolutionsWithMinimumPackages(int minPackages);

    void deleteById(String solutionId);

    void deleteOlderThan(Instant cutoffDate);

    long count();

    boolean existsById(String solutionId);

    boolean existsByRequestId(String requestId);
}