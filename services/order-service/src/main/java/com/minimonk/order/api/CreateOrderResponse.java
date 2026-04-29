package com.minimonk.order.api;

import com.minimonk.order.OrderStatus;

import java.util.UUID;

public record CreateOrderResponse(UUID orderId, OrderStatus status) {
}
