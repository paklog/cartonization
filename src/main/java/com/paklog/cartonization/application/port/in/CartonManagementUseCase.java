package com.paklog.cartonization.application.port.in;

import com.paklog.cartonization.application.port.in.command.CreateCartonCommand;
import com.paklog.cartonization.domain.model.aggregate.Carton;

import java.util.List;

public interface CartonManagementUseCase {

    Carton createCarton(CreateCartonCommand command);

    Carton getCartonById(String cartonId);

    List<Carton> listAllCartons();

    List<Carton> listActiveCartons();

    Carton updateCarton(String cartonId, CreateCartonCommand command);

    void deactivateCarton(String cartonId);
}