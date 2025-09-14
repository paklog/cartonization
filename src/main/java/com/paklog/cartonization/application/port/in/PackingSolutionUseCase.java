package com.paklog.cartonization.application.port.in;

import com.paklog.cartonization.application.port.in.command.CalculatePackingSolutionCommand;
import com.paklog.cartonization.domain.model.entity.PackingSolution;

public interface PackingSolutionUseCase {
    PackingSolution calculate(CalculatePackingSolutionCommand command);
}