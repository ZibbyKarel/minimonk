package com.minimonk.order.api;

import com.minimonk.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderOverviewDto(
        UUID orderId,
        UUID customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        long itemCount,
        Instant createdAt,
        Instant updatedAt
) {
}
