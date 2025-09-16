package com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.repository;

import com.paklog.cartonization.application.port.out.CartonRepository;
import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.valueobject.CartonId;
import com.paklog.cartonization.domain.model.valueobject.CartonStatus;
import com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.document.CartonDocument;
import com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.mapper.CartonDocumentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Primary
public class MongoCartonRepository implements CartonRepository {

    private static final Logger log = LoggerFactory.getLogger(MongoCartonRepository.class);
    
    private final SpringDataMongoCartonRepository springDataRepository;
    private final CartonDocumentMapper mapper;
    
    public MongoCartonRepository(SpringDataMongoCartonRepository springDataRepository, CartonDocumentMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    @CacheEvict(value = {"cartons", "carton-by-id", "active-cartons"}, allEntries = true)
    public Carton save(Carton carton) {
        log.debug("Saving carton with ID: {}", carton.getId());

        CartonDocument document = mapper.toDocument(carton);
        CartonDocument saved = springDataRepository.save(document);

        // Publish domain events after successful save
        carton.pullDomainEvents().forEach(event -> {
            log.info("Publishing domain event: {}", event.getClass().getSimpleName());
            // Event publishing logic would go here
        });

        return mapper.toDomain(saved);
    }

    @Override
    @Cacheable(value = "carton-by-id", key = "#id.value")
    public Optional<Carton> findById(CartonId id) {
        log.debug("Finding carton by ID: {}", id);
        return springDataRepository.findById(id.getValue())
            .map(mapper::toDomain);
    }

    @Override
    @Cacheable(value = "cartons")
    public List<Carton> findAll() {
        log.debug("Finding all cartons");
        return springDataRepository.findAll().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "active-cartons")
    public List<Carton> findAllActive() {
        log.debug("Finding all active cartons");
        return springDataRepository.findByStatus(CartonStatus.ACTIVE.name()).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = {"cartons", "carton-by-id", "active-cartons"}, allEntries = true)
    public void deleteById(CartonId id) {
        log.info("Deleting carton with ID: {}", id);
        springDataRepository.deleteById(id.getValue());
    }

    @Override
    public boolean existsById(CartonId id) {
        return springDataRepository.existsById(id.getValue());
    }

    @Override
    public long count() {
        return springDataRepository.count();
    }
}