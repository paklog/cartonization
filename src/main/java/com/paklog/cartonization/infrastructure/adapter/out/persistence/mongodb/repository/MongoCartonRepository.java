package com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.repository;

import com.paklog.cartonization.application.port.out.CartonRepository;
import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.valueobject.CartonId;
import com.paklog.cartonization.domain.model.valueobject.CartonStatus;
import com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.document.CartonDocument;
import com.paklog.cartonization.infrastructure.adapter.out.persistence.mongodb.mapper.CartonDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MongoCartonRepository implements CartonRepository {

    private final SpringDataMongoCartonRepository springDataRepository;
    private final CartonDocumentMapper mapper;

    @Override
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
    public Optional<Carton> findById(CartonId id) {
        log.debug("Finding carton by ID: {}", id);
        return springDataRepository.findById(id.getValue())
            .map(mapper::toDomain);
    }

    @Override
    public List<Carton> findAll() {
        log.debug("Finding all cartons");
        return springDataRepository.findAll().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public List<Carton> findAllActive() {
        log.debug("Finding all active cartons");
        return springDataRepository.findByStatus(CartonStatus.ACTIVE.name()).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
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