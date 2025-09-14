package com.paklog.cartonization.application.port.out;

import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.valueobject.CartonId;

import java.util.List;
import java.util.Optional;

public interface CartonRepository {

    Carton save(Carton carton);

    Optional<Carton> findById(CartonId id);

    List<Carton> findAll();

    List<Carton> findAllActive();

    void deleteById(CartonId id);

    boolean existsById(CartonId id);

    long count();
}