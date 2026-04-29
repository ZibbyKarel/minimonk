package com.minimonk.order.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderItemRequest(
        @NotNull UUID productId,
        @NotBlank String sku,
        @NotBlank String name,
        @Min(1) int quantity,
        @NotNull BigDecimal unitPrice
) {
}
