package com.paklog.cartonization.application.service;

import com.paklog.cartonization.domain.model.valueobject.ItemToPack;
import com.paklog.cartonization.domain.model.valueobject.ItemWithDimensions;

import java.util.List;

public interface ProductDimensionEnricher {
    List<ItemWithDimensions> enrichItems(List<ItemToPack> items);
}
