package com.minimonk.events;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderCreatedPayload(
        UUID orderId,
        UUID customerId,
        String paymentCardNumber,
        BigDecimal totalAmount,
        List<OrderItemPayload> items
) {
}
