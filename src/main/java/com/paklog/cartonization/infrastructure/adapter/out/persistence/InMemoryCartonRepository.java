package com.paklog.cartonization.infrastructure.adapter.out.persistence;

import com.paklog.cartonization.application.port.out.CartonRepository;
import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.valueobject.CartonId;
import com.paklog.cartonization.domain.model.valueobject.CartonStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryCartonRepository implements CartonRepository {

    private final Map<String, Carton> cartons = new ConcurrentHashMap<>();

    @Override
    public Carton save(Carton carton) {
        cartons.put(carton.getId().getValue(), carton);
        return carton;
    }

    @Override
    public Optional<Carton> findById(CartonId id) {
        return Optional.ofNullable(cartons.get(id.getValue()));
    }

    @Override
    public List<Carton> findAll() {
        return List.copyOf(cartons.values());
    }

    @Override
    public List<Carton> findAllActive() {
        return cartons.values().stream()
            .filter(carton -> carton.getStatus() == CartonStatus.ACTIVE)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(CartonId id) {
        cartons.remove(id.getValue());
    }

    @Override
    public boolean existsById(CartonId id) {
        return cartons.containsKey(id.getValue());
    }

    @Override
    public long count() {
        return cartons.size();
    }
}