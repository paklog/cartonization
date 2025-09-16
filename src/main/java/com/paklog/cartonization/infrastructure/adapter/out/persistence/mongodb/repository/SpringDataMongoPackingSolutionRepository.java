package com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.repository;

import com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.document.PackingSolutionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataMongoPackingSolutionRepository extends MongoRepository<PackingSolutionDocument, String> {

    Optional<PackingSolutionDocument> findByRequestId(String requestId);

    List<PackingSolutionDocument> findByOrderId(String orderId);

    @Query(value = "{}", sort = "{ 'createdAt': -1 }")
    List<PackingSolutionDocument> findRecentSolutions(org.springframework.data.domain.Pageable pageable);

    List<PackingSolutionDocument> findByCreatedAtAfter(Instant since);

    @Query("{ 'packages': { $size: { $gte: ?0 } } }")
    List<PackingSolutionDocument> findByPackageCountGreaterThanEqual(int minPackages);

    void deleteByCreatedAtBefore(Instant cutoffDate);

    boolean existsByRequestId(String requestId);

    @Query(value = "{ 'createdAt': { $gte: ?0, $lte: ?1 } }", count = true)
    long countByCreatedAtBetween(Instant start, Instant end);
}