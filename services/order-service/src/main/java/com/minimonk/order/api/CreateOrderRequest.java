package com.minimonk.order.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        UUID customerId,
        @NotBlank String paymentCardNumber,
        @NotEmpty List<@Valid CreateOrderItemRequest> items
) {
}
