package com.minimonk.warehouse.api;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductDto(
        UUID id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        int availableQuantity,
        int reservedQuantity
) {
}
