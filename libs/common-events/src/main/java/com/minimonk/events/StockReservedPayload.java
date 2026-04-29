package com.minimonk.events;

import java.util.UUID;

public record StockReservedPayload(UUID orderId, UUID customerId, String paymentCardNumber) {
}
