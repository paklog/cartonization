package com.paklog.cartonization.application.service;

import com.paklog.cartonization.application.port.in.CartonManagementUseCase;
import com.paklog.cartonization.application.port.in.command.CreateCartonCommand;
import com.paklog.cartonization.application.port.out.CartonRepository;
import com.paklog.cartonization.domain.exception.CartonNotFoundException;
import com.paklog.cartonization.domain.model.aggregate.Carton;
import com.paklog.cartonization.domain.model.valueobject.CartonId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CartonManagementService implements CartonManagementUseCase {

    private static final Logger log = LoggerFactory.getLogger(CartonManagementService.class);
    
    private final CartonRepository cartonRepository;
    
    public CartonManagementService(CartonRepository cartonRepository) {
        this.cartonRepository = cartonRepository;
    }

    @Override
    public Carton createCarton(CreateCartonCommand command) {
        log.info("Creating new carton: {}", command.name());

        Carton carton = Carton.create(
            command.name(),
            command.dimensions(),
            command.maxWeight()
        );

        Carton savedCarton = cartonRepository.save(carton);
        log.info("Successfully created carton with ID: {}", savedCarton.getId().getValue());

        return savedCarton;
    }

    @Override
    @Transactional(readOnly = true)
    public Carton getCartonById(String cartonId) {
        log.debug("Retrieving carton by ID: {}", cartonId);

        return cartonRepository.findById(CartonId.of(cartonId))
            .orElseThrow(() -> new CartonNotFoundException("Carton not found: " + cartonId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Carton> listAllCartons() {
        log.debug("Listing all cartons");
        return cartonRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Carton> listActiveCartons() {
        log.debug("Listing active cartons");
        return cartonRepository.findAllActive();
    }

    @Override
    public Carton updateCarton(String cartonId, CreateCartonCommand command) {
        log.info("Updating carton: {}", cartonId);

        Carton existingCarton = getCartonById(cartonId);

        // For now, we'll recreate the carton with new values
        // In a more complex scenario, we'd have update methods on the aggregate
        Carton updatedCarton = Carton.create(
            command.name(),
            command.dimensions(),
            command.maxWeight()
        );

        // Copy the ID from the existing carton
        try {
            java.lang.reflect.Field idField = Carton.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(updatedCarton, existingCarton.getId());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("Failed to copy carton ID during update", e);
            throw new RuntimeException("Failed to update carton", e);
        }

        Carton savedCarton = cartonRepository.save(updatedCarton);
        log.info("Successfully updated carton: {}", cartonId);

        return savedCarton;
    }

    @Override
    public void deactivateCarton(String cartonId) {
        log.info("Deactivating carton: {}", cartonId);

        Carton carton = getCartonById(cartonId);
        carton.deactivate();
        cartonRepository.save(carton);

        log.info("Successfully deactivated carton: {}", cartonId);
    }
}