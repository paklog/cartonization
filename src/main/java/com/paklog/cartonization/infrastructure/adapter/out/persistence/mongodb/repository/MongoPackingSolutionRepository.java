package com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.repository;

import com.paklog.cartonization.application.port.out.PackingSolutionRepository;
import com.paklog.cartonization.domain.model.entity.PackingSolution;
import com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.document.PackingSolutionDocument;
import com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.mapper.PackingSolutionDocumentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MongoPackingSolutionRepository implements PackingSolutionRepository {

    private static final Logger log = LoggerFactory.getLogger(MongoPackingSolutionRepository.class);
    
    private final SpringDataMongoPackingSolutionRepository springDataRepository;
    private final PackingSolutionDocumentMapper mapper;

    public MongoPackingSolutionRepository(SpringDataMongoPackingSolutionRepository springDataRepository, 
                                        PackingSolutionDocumentMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    @CacheEvict(value = {"packing-solutions", "packing-cache"}, allEntries = true)
    public PackingSolution save(PackingSolution solution) {
        try {
            log.debug("Saving packing solution: {}", solution.getSolutionId());
            
            PackingSolutionDocument document = mapper.toDocument(solution);
            PackingSolutionDocument savedDocument = springDataRepository.save(document);
            
            log.info("Successfully saved packing solution: {}", savedDocument.getSolutionId());
            return mapper.fromDocument(savedDocument);
            
        } catch (Exception e) {
            log.error("Failed to save packing solution: {}", solution.getSolutionId(), e);
            throw new RuntimeException("Failed to save packing solution", e);
        }
    }

    @Override
    @Cacheable(value = "packing-solutions", key = "#solutionId")
    public Optional<PackingSolution> findById(String solutionId) {
        try {
            log.debug("Finding packing solution by ID: {}", solutionId);
            
            return springDataRepository.findById(solutionId)
                .map(mapper::fromDocument);
                
        } catch (Exception e) {
            log.error("Failed to find packing solution by ID: {}", solutionId, e);
            return Optional.empty();
        }
    }

    @Override
    @Cacheable(value = "packing-cache", key = "'request:' + #requestId")
    public Optional<PackingSolution> findByRequestId(String requestId) {
        try {
            log.debug("Finding packing solution by request ID: {}", requestId);
            
            return springDataRepository.findByRequestId(requestId)
                .map(mapper::fromDocument);
                
        } catch (Exception e) {
            log.error("Failed to find packing solution by request ID: {}", requestId, e);
            return Optional.empty();
        }
    }

    @Override
    public List<PackingSolution> findByOrderId(String orderId) {
        try {
            log.debug("Finding packing solutions by order ID: {}", orderId);
            
            return springDataRepository.findByOrderId(orderId).stream()
                .map(mapper::fromDocument)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Failed to find packing solutions by order ID: {}", orderId, e);
            return List.of();
        }
    }

    @Override
    public List<PackingSolution> findRecentSolutions(int limit) {
        try {
            log.debug("Finding {} recent packing solutions", limit);
            
            PageRequest pageRequest = PageRequest.of(0, limit);
            return springDataRepository.findRecentSolutions(pageRequest).stream()
                .map(mapper::fromDocument)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Failed to find recent packing solutions", e);
            return List.of();
        }
    }

    @Override
    public List<PackingSolution> findSolutionsCreatedAfter(Instant since) {
        try {
            log.debug("Finding packing solutions created after: {}", since);
            
            return springDataRepository.findByCreatedAtAfter(since).stream()
                .map(mapper::fromDocument)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Failed to find packing solutions created after: {}", since, e);
            return List.of();
        }
    }

    @Override
    public List<PackingSolution> findSolutionsWithMinimumPackages(int minPackages) {
        try {
            log.debug("Finding packing solutions with minimum {} packages", minPackages);
            
            return springDataRepository.findByPackageCountGreaterThanEqual(minPackages).stream()
                .map(mapper::fromDocument)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Failed to find packing solutions with minimum packages: {}", minPackages, e);
            return List.of();
        }
    }

    @Override
    @CacheEvict(value = {"packing-solutions", "packing-cache"}, allEntries = true)
    public void deleteById(String solutionId) {
        try {
            log.debug("Deleting packing solution: {}", solutionId);
            
            springDataRepository.deleteById(solutionId);
            
            log.info("Successfully deleted packing solution: {}", solutionId);
            
        } catch (Exception e) {
            log.error("Failed to delete packing solution: {}", solutionId, e);
            throw new RuntimeException("Failed to delete packing solution", e);
        }
    }

    @Override
    @CacheEvict(value = {"packing-solutions", "packing-cache"}, allEntries = true)
    public void deleteOlderThan(Instant cutoffDate) {
        try {
            log.debug("Deleting packing solutions older than: {}", cutoffDate);
            
            springDataRepository.deleteByCreatedAtBefore(cutoffDate);
            
            log.info("Successfully deleted packing solutions older than: {}", cutoffDate);
            
        } catch (Exception e) {
            log.error("Failed to delete old packing solutions", e);
            throw new RuntimeException("Failed to delete old packing solutions", e);
        }
    }

    @Override
    public long count() {
        try {
            return springDataRepository.count();
        } catch (Exception e) {
            log.error("Failed to count packing solutions", e);
            return 0;
        }
    }

    @Override
    public boolean existsById(String solutionId) {
        try {
            return springDataRepository.existsById(solutionId);
        } catch (Exception e) {
            log.error("Failed to check existence of packing solution: {}", solutionId, e);
            return false;
        }
    }

    @Override
    public boolean existsByRequestId(String requestId) {
        try {
            return springDataRepository.existsByRequestId(requestId);
        } catch (Exception e) {
            log.error("Failed to check existence by request ID: {}", requestId, e);
            return false;
        }
    }
}